import { useEffect, useState } from 'react'
import { api } from '../api'
import Card from '../components/Card'
import Button from '../components/Button'
import DashboardLayout from '../layout/DashboardLayout'

export default function FindTutors(){
  const [tutors, setTutors] = useState([])
  const [slots, setSlots] = useState([])
  const [loading, setLoading] = useState(false)
  const [showBookingModal, setShowBookingModal] = useState(false)
  const [modalTutor, setModalTutor] = useState(null)
  const [modalSlots, setModalSlots] = useState([])
  const [selectedSlotId, setSelectedSlotId] = useState(null)
  const [modalSubject, setModalSubject] = useState('')
  const [bookingLoading, setBookingLoading] = useState(false)

  const safeUser = () => { try{ return JSON.parse(localStorage.getItem('user')) }catch(e){return null} }

  const loadTutors = async (params = {}) => {
    setLoading(true)
    try{
      const qs = new URLSearchParams(params).toString()
      const r = await api.get('/api/student/search' + (qs ? '?'+qs : '')).then(x=>x.data)
      setTutors(r || [])
    }catch(e){ console.error(e) }
    setLoading(false)
  }

  const loadSlots = async ()=>{
    try{ const s = await api.get('/api/slot/available').then(x=>x.data); setSlots(s || []) }catch(e){ console.error(e) }
  }

  useEffect(()=>{ loadTutors(); loadSlots() }, [])

  // listen for slot updates from other tabs/pages
  useEffect(()=>{
    const handler = (e) => {
      if(e.key === 'slots-updated') loadSlots()
    }
    window.addEventListener('storage', handler)
    return () => window.removeEventListener('storage', handler)
  }, [])

  const openBookingModal = (tutor, preselectedSlot) => {
    const user = safeUser()
    if(!user || user.role !== 'STUDENT'){ alert('Please login as a student to book'); return }
    setModalTutor(tutor)
    if(preselectedSlot){
      setModalSlots([preselectedSlot])
      setSelectedSlotId(preselectedSlot.id)
    } else {
      const forTutor = (slots || []).filter(s => s.tutor && s.tutor.id === tutor.id)
      setModalSlots(forTutor)
      setSelectedSlotId(forTutor.length ? forTutor[0].id : null)
    }
    let sub = ''
    if(tutor?.subjects){ try{ sub = Array.isArray(tutor.subjects) ? (tutor.subjects[0]||'') : (tutor.subjects||'').split(',')[0] }catch(e){} }
    setModalSubject(sub || '')
    setShowBookingModal(true)
  }

  const closeBookingModal = () => {
    setShowBookingModal(false)
    setModalTutor(null)
    setModalSlots([])
    setSelectedSlotId(null)
    setModalSubject('')
    setBookingLoading(false)
  }

  const confirmBooking = async () => {
    if(!modalTutor || !selectedSlotId) return alert('Select a slot to book')
    setBookingLoading(true)
    try{
      const user = safeUser()
      const payload = { tutorId: modalTutor.id, studentId: user.userId, slotId: selectedSlotId, subject: modalSubject }
      await api.post('/api/student/request', payload)
      alert('Booking request created')
      setSlots(prev => prev.filter(s=>s.id !== selectedSlotId))
      try{ localStorage.setItem('slots-updated', String(Date.now())) }catch(e){}
      closeBookingModal()
    }catch(e){ console.error(e); alert('Failed to create booking request') }finally{ setBookingLoading(false) }
  }

  return (
    <DashboardLayout title="Find Tutors">
      {showBookingModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div className="absolute inset-0 bg-black opacity-40" onClick={closeBookingModal} />
          <div className="bg-white rounded-lg p-6 z-60 w-full max-w-lg shadow-lg relative">
            <h3 className="text-lg font-semibold">Book with {modalTutor?.fullName || modalTutor?.user?.email || 'Tutor'}</h3>
            <div className="mt-3">
              <label className="label">Choose Slot</label>
              <select className="input" value={selectedSlotId || ''} onChange={e=>setSelectedSlotId(Number(e.target.value))}>
                {(modalSlots || []).map(s => (
                  <option key={s.id} value={s.id}>{s.date} {s.start}-{s.end}</option>
                ))}
              </select>
            </div>
            <div className="mt-3">
              <label className="label">Subject</label>
              <input className="input" value={modalSubject} onChange={e=>setModalSubject(e.target.value)} placeholder="Subject/topic" />
            </div>
            <div className="mt-4 flex justify-end gap-2">
              <button className="btn btn-ghost" onClick={closeBookingModal}>Cancel</button>
              <button className="btn btn-primary" onClick={confirmBooking} disabled={bookingLoading}>{bookingLoading ? 'Booking...' : 'Confirm Booking'}</button>
            </div>
          </div>
        </div>
      )}
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">Find Tutors</h2>
          <div>
            <Button onClick={()=>{ loadTutors(); loadSlots() }}>Refresh</Button>
          </div>
        </div>

        {loading && <div className="text-center p-6">Loading tutors...</div>}

        <div className="grid md:grid-cols-3 gap-6">
          {tutors.map(t => (
            <Card key={t.id} className="p-4">
              <div className="flex items-start gap-4">
                <div className="w-16 h-16 bg-gray-100 rounded overflow-hidden">
                  <img src={t.photoUrl || 'https://via.placeholder.com/64'} alt="tutor" className="object-cover w-full h-full" />
                </div>
                <div className="flex-1">
                  <div className="font-semibold text-lg">{t.fullName || (t.user?.name) || 'Tutor'}</div>
                  <div className="text-sm text-gray-600">{Array.isArray(t.subjects) ? t.subjects.join(', ') : t.subjects}</div>
                  <div className="text-sm text-gray-600">Rate: {t.ratePerHour ? ('$'+t.ratePerHour) : '—'}</div>
                </div>
              </div>

              <div className="mt-3">
                <div className="text-sm font-semibold">Open Slots</div>
                <div className="mt-2 space-y-2">
                  {slots.filter(s => s.tutor && s.tutor.id === t.id).length === 0 ? (
                    <div className="text-xs text-gray-500">No open slots</div>
                  ) : (
                    slots.filter(s => s.tutor && s.tutor.id === t.id).map(s => (
                      <div key={s.id} className="flex items-center justify-between p-2 border rounded">
                        <div className="text-sm">{s.date} {s.start}-{s.end}</div>
                        <div>
                          <Button onClick={()=>openBookingModal(t,s)} className="btn-sm">Book</Button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </DashboardLayout>
  )
}
