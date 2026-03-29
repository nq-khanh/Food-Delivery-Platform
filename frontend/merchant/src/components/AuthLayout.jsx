import styles from './AuthLayout.module.css'

/**
 * AuthLayout — 2-column layout for Login & Register pages.
 * Left: branding/hero  |  Right: form
 */
export default function AuthLayout({ children }) {
  return (
    <div className={styles.wrapper}>
      {/* Left — Hero */}
      <div className={styles.hero}>
        <div className={styles.heroContent}>
          <div className={styles.logo}>
            <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
              <rect width="40" height="40" rx="12" fill="url(#g1)" />
              <path d="M10 28 Q12 15 20 14 Q28 15 30 28" stroke="white" strokeWidth="2.5" strokeLinecap="round" fill="none"/>
              <circle cx="20" cy="11" r="3" fill="white"/>
              <path d="M14 28 h12" stroke="white" strokeWidth="2.5" strokeLinecap="round"/>
              <defs>
                <linearGradient id="g1" x1="0" y1="0" x2="40" y2="40">
                  <stop offset="0%" stopColor="#ff6b35"/>
                  <stop offset="100%" stopColor="#f7931e"/>
                </linearGradient>
              </defs>
            </svg>
            <span className={styles.logoText}>FoodDelivery</span>
          </div>
          <h1 className={styles.heroTitle}>
            Đưa ẩm thực của bạn<br />
            <span className={styles.heroAccent}>đến mọi người</span>
          </h1>
          <p className={styles.heroSub}>
            Tham gia cùng hàng nghìn nhà hàng đang phát triển mạnh trên nền tảng của chúng tôi.
          </p>
          <div className={styles.stats}>
            <div className={styles.stat}>
              <span className={styles.statNum}>12K+</span>
              <span className={styles.statLabel}>Nhà hàng</span>
            </div>
            <div className={styles.statDivider} />
            <div className={styles.stat}>
              <span className={styles.statNum}>850K+</span>
              <span className={styles.statLabel}>Đơn hàng/tháng</span>
            </div>
            <div className={styles.statDivider} />
            <div className={styles.stat}>
              <span className={styles.statNum}>4.8★</span>
              <span className={styles.statLabel}>Đánh giá TB</span>
            </div>
          </div>
          {/* Decorative blobs */}
          <div className={styles.blob1} />
          <div className={styles.blob2} />
        </div>
      </div>

      {/* Right — Form */}
      <div className={styles.formPanel}>
        <div className={styles.formWrapper}>
          {children}
        </div>
      </div>
    </div>
  )
}
