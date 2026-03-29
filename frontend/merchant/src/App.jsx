import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import SetupRestaurantPage from './pages/SetupRestaurantPage'
import PendingApprovalPage from './pages/PendingApprovalPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/setup" element={<SetupRestaurantPage />} />
      <Route path="/pending" element={<PendingApprovalPage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App