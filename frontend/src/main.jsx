// src/main.jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom'
import './styles/index.css'

// Pages (use exact import patterns requested)
import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'

import StudentDashboard from './pages/StudentDashboard'
import StudentProfile from './pages/StudentProfile'
import StudentRecords from './pages/StudentRecords'

import TutorDashboard from './pages/TutorDashboard'
import TutorProfile from './pages/TutorProfile'
import TutorView from './pages/TutorView'
import TutorManage from './pages/TutorManage'
import FindTutors from './pages/FindTutors'

import AdminDashboard from './pages/AdminDashboard'
import AdminProfile from './pages/AdminProfile'
import AdminManageUsers from './pages/AdminManageUsers'
import Nav from './components/Nav'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      {/* Render Nav on public routes only (hide on role dashboards) */}
      <RouteAwareNav />
      <Routes>
        <Route path="/" element={<Landing/>} />
        <Route path="/login" element={<Login/>} />
        <Route path="/register" element={<Register/>} />

        <Route path="/student/dashboard" element={<StudentDashboard/>} />
        <Route path="/student/findtutors" element={<FindTutors/>} />
        <Route path="/student/records" element={<StudentRecords/>} />
        <Route path="/student/profile" element={<StudentProfile/>} />

        <Route path="/tutor/dashboard" element={<TutorDashboard/>} />
        <Route path="/tutor/manage" element={<TutorManage/>} />
        <Route path="/tutor/profile" element={<TutorProfile/>} />
        <Route path="/tutor/view/:id" element={<TutorView/>} />

        <Route path="/admin/dashboard" element={<AdminDashboard/>} />
        <Route path="/admin/manageusers" element={<AdminManageUsers/>} />
        <Route path="/admin/profile" element={<AdminProfile/>} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
)

function RouteAwareNav(){
  const { pathname } = useLocation()
  // hide nav for any internal dashboard routes
  if(pathname.startsWith('/student') || pathname.startsWith('/tutor') || pathname.startsWith('/admin')) return null
  return <Nav />
}
