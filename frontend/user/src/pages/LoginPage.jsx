import { Link } from 'react-router-dom';

const LoginPage = () => {
  return (
    <div style={{ 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center', 
      minHeight: '80vh',
      backgroundColor: 'var(--bg-color)',
      padding: '2rem'
    }}>
      <div style={{ 
        background: 'var(--white)', 
        padding: '3rem', 
        borderRadius: 'var(--radius-lg)',
        boxShadow: 'var(--shadow-lg)',
        width: '100%',
        maxWidth: '450px'
      }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 className="title" style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Đăng Nhập</h1>
          <p style={{ color: 'var(--text-light)' }}>Đăng nhập vào tài khoản của bạn để tiếp tục.</p>
        </div>

        <form onSubmit={(e) => e.preventDefault()}>
          <div className="form-group">
            <label className="form-label" htmlFor="email">Email / Số điện thoại</label>
            <input 
              type="text" 
              id="email" 
              className="form-control" 
              placeholder="user@example.com" 
            />
          </div>

          <div className="form-group" style={{ marginBottom: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
              <label className="form-label" htmlFor="password" style={{ margin: 0 }}>Mật khẩu</label>
              <a href="#" style={{ fontSize: '0.875rem', color: 'var(--primary-color)' }}>Quên mật khẩu?</a>
            </div>
            <input 
              type="password" 
              id="password" 
              className="form-control" 
              placeholder="••••••••" 
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '1rem' }}>
            Đăng Nhập
          </button>
        </form>

        <div style={{ marginTop: '2rem', textAlign: 'center', color: 'var(--text-light)' }}>
          Chưa có tài khoản? <Link to="#" style={{ color: 'var(--primary-color)', fontWeight: 600 }}>Đăng ký ngay</Link>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
