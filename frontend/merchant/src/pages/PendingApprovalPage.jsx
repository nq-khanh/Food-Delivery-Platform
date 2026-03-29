import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import styles from './PendingApprovalPage.module.css'

export default function PendingApprovalPage() {
  const [restaurant, setRestaurant] = useState(null)
  const [dots, setDots] = useState('.')

  useEffect(() => {
    const raw = localStorage.getItem('pending_restaurant')
    if (raw) setRestaurant(JSON.parse(raw))
  }, [])

  // Animated dots
  useEffect(() => {
    const id = setInterval(() => setDots((d) => (d.length >= 3 ? '.' : d + '.')), 600)
    return () => clearInterval(id)
  }, [])

  return (
    <div className={styles.page}>
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.logo}>
          <svg width="32" height="32" viewBox="0 0 40 40" fill="none">
            <rect width="40" height="40" rx="12" fill="url(#g3)"/>
            <path d="M10 28 Q12 15 20 14 Q28 15 30 28" stroke="white" strokeWidth="2.5" strokeLinecap="round" fill="none"/>
            <circle cx="20" cy="11" r="3" fill="white"/>
            <path d="M14 28 h12" stroke="white" strokeWidth="2.5" strokeLinecap="round"/>
            <defs>
              <linearGradient id="g3" x1="0" y1="0" x2="40" y2="40">
                <stop offset="0%" stopColor="#ff6b35"/>
                <stop offset="100%" stopColor="#f7931e"/>
              </linearGradient>
            </defs>
          </svg>
          <span className={styles.logoText}>FoodDelivery</span>
        </div>
      </div>

      {/* Main */}
      <div className={styles.content}>
        {/* Pulse animation */}
        <div className={styles.pulseWrapper}>
          <div className={styles.pulseRing} />
          <div className={styles.pulseRing2} />
          <div className={styles.iconCircle}>
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <path d="M24 8C15.16 8 8 15.16 8 24s7.16 16 16 16 16-7.16 16-16S32.84 8 24 8z" fill="url(#clock-g)"/>
              <path d="M24 16v8l5 5" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
              <defs>
                <linearGradient id="clock-g" x1="8" y1="8" x2="40" y2="40">
                  <stop stopColor="#ff6b35"/>
                  <stop offset="1" stopColor="#f7931e"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
        </div>

        <div className={styles.badge}>
          <span className={styles.badgeDot} />
          Đang chờ phê duyệt
        </div>

        <h1 className={styles.title}>
          Đăng ký đã được gửi!
        </h1>

        {restaurant && (
          <p className={styles.restaurantName}>🍽️ {restaurant.name}</p>
        )}

        <p className={styles.desc}>
          Đội ngũ quản trị đang xem xét hồ sơ của bạn{dots}<br />
          Quá trình thường mất <strong>1–3 ngày làm việc</strong>.
        </p>

        {/* Steps */}
        <div className={styles.timeline}>
          <div className={`${styles.timelineItem} ${styles.done}`}>
            <div className={styles.timelineDot}>✓</div>
            <div className={styles.timelineText}>
              <span className={styles.timelineTitle}>Đã gửi hồ sơ</span>
              <span className={styles.timelineSub}>Thông tin nhà hàng đã được nhận</span>
            </div>
          </div>
          <div className={`${styles.timelineItem} ${styles.active}`}>
            <div className={styles.timelineDot}>
              <span className={styles.timelineSpinner} />
            </div>
            <div className={styles.timelineText}>
              <span className={styles.timelineTitle}>Đang xét duyệt</span>
              <span className={styles.timelineSub}>Quản trị viên đang kiểm tra thông tin</span>
            </div>
          </div>
          <div className={styles.timelineItem}>
            <div className={styles.timelineDot}>3</div>
            <div className={styles.timelineText}>
              <span className={styles.timelineTitle}>Kích hoạt tài khoản</span>
              <span className={styles.timelineSub}>Bắt đầu nhận đơn hàng từ khách</span>
            </div>
          </div>
        </div>

        {/* Info cards */}
        <div className={styles.infoCards}>
          <div className={styles.infoCard}>
            <span className={styles.infoEmoji}>📧</span>
            <div>
              <p className={styles.infoTitle}>Email thông báo</p>
              <p className={styles.infoSub}>Chúng tôi sẽ gửi email khi hồ sơ được duyệt hoặc cần bổ sung</p>
            </div>
          </div>
          <div className={styles.infoCard}>
            <span className={styles.infoEmoji}>📞</span>
            <div>
              <p className={styles.infoTitle}>Hỗ trợ</p>
              <p className={styles.infoSub}>Hotline: <strong>1800 6789</strong> (8:00 – 22:00 hàng ngày)</p>
            </div>
          </div>
        </div>

        <div className={styles.actions}>
          <Link to="/login" id="back-to-login" className="btn btn-ghost">
            ← Quay lại đăng nhập
          </Link>
        </div>
      </div>
    </div>
  )
}
