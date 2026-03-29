import { Outlet, Link } from 'react-router-dom';

const MainLayout = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {/* Header Giả Lập */}
      <header style={{ backgroundColor: '#fff', borderBottom: '1px solid #eee', padding: '1rem 0' }}>
        <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ fontWeight: 'bold', fontSize: '1.5rem', color: 'var(--primary-color)' }}>
             Food<span style={{color: '#2f3542'}}>Delivery</span>
          </div>
          <nav style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
            <Link to="/" style={{ fontWeight: 500 }}>Trang chủ</Link>
            <a href="#" style={{ fontWeight: 500 }}>Nhà hàng</a>
            <a href="#" style={{ fontWeight: 500 }}>Ưu đãi</a>
            <Link to="/login" className="btn btn-primary" style={{ padding: '0.5rem 1rem' }}>
              Đăng nhập
            </Link>
          </nav>
        </div>
      </header>

      {/* Main Content (Trang con sẽ render tại Outlet) */}
      <main style={{ flex: 1 }}>
        <Outlet />
      </main>

      {/* Footer Giả Lập */}
      <footer style={{ backgroundColor: '#2f3542', color: '#fff', padding: '3rem 0', marginTop: 'auto' }}>
        <div className="container" style={{ textAlign: 'center' }}>
          <p>&copy; {new Date().getFullYear()} Food Delivery Platform. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default MainLayout;
