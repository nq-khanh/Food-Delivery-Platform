const HomePage = () => {
  return (
    <div style={{ padding: '4rem 0' }}>
      <div className="container" style={{ textAlign: 'center', maxWidth: '800px' }}>
        <h1 className="title" style={{ fontSize: '3rem', marginBottom: '1.5rem' }}>
          Giao Đồ Ăn <span style={{ color: 'var(--primary-color)' }}>Nhanh Chóng</span>
        </h1>
        <p className="subtitle" style={{ fontSize: '1.25rem', marginBottom: '2.5rem' }}>
          Khám phá những món ăn ngon từ hàng ngàn nhà hàng quanh bạn. Đặt món ngay và nhận giao hàng tận nơi.
        </p>
        
        <div style={{ 
          display: 'flex', 
          background: 'var(--white)', 
          padding: '0.5rem', 
          borderRadius: '50px',
          boxShadow: 'var(--shadow-lg)'
        }}>
          <input 
            type="text" 
            placeholder="Nhập địa chỉ của bạn để tìm nhà hàng..." 
            style={{ 
              flex: 1, 
              border: 'none', 
              padding: '0 1.5rem', 
              outline: 'none',
              borderRadius: '50px',
              fontSize: '1rem'
            }} 
          />
          <button className="btn btn-primary" style={{ borderRadius: '50px' }}>
            Tìm Kiếm Món Ăn
          </button>
        </div>

        <div style={{ marginTop: '4rem' }}>
          {/* Mảng giả lập danh mục */}
          <h2 style={{ marginBottom: '2rem' }}>Danh Mục Món Ăn Nổi Bật</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1.5rem' }}>
             {[1,2,3,4].map((item) => (
                <div key={item} style={{ 
                  padding: '2rem', 
                  background: 'var(--white)', 
                  borderRadius: 'var(--radius-md)',
                  boxShadow: 'var(--shadow-sm)',
                  cursor: 'pointer'
                }}>
                  <h3>Món Gà {item}</h3>
                </div>
             ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
