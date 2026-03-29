import { createBrowserRouter } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {
        index: true, // Đường dẫn '/'
        element: <HomePage />,
      },
      {
        path: 'login', // Đường dẫn '/login'
        element: <LoginPage />,
      },
    ],
  },
  // Có thể tách riêng layout cho trang Login/Register nếu không muốn Header/Footer
]);

export default router;
