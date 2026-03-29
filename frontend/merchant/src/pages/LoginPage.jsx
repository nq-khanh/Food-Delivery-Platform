import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import { login } from '../services/authService'
import styles from './AuthPage.module.css'

export default function LoginPage() {
  const navigate = useNavigate()
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm()

  const onSubmit = async (data) => {
    try {
      setLoading(true)
      setServerError('')
      await login(data)
      navigate('/setup')
    } catch (err) {
      setServerError(err.message || 'Đăng nhập thất bại. Vui lòng thử lại.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout>
      <div className={styles.formHeader}>
        <h2 className={styles.formTitle}>Chào mừng trở lại</h2>
        <p className={styles.formSub}>Đăng nhập vào Merchant Portal của bạn</p>
      </div>

      <form id="login-form" onSubmit={handleSubmit(onSubmit)} noValidate className={styles.form}>
        {/* Email */}
        <div className="form-group">
          <label className="form-label" htmlFor="login-email">Email</label>
          <input
            id="login-email"
            type="email"
            placeholder="your@email.com"
            className={`input-field ${errors.email ? 'error' : ''}`}
            {...register('email', {
              required: 'Email là bắt buộc',
              pattern: { value: /^\S+@\S+$/i, message: 'Email không hợp lệ' },
            })}
          />
          {errors.email && <span className="form-error">⚠ {errors.email.message}</span>}
        </div>

        {/* Password */}
        <div className="form-group">
          <div className={styles.labelRow}>
            <label className="form-label" htmlFor="login-password">Mật khẩu</label>
            <a href="#" className={styles.forgotLink}>Quên mật khẩu?</a>
          </div>
          <div className={styles.inputWrapper}>
            <input
              id="login-password"
              type={showPassword ? 'text' : 'password'}
              placeholder="••••••••"
              className={`input-field ${errors.password ? 'error' : ''}`}
              {...register('password', {
                required: 'Mật khẩu là bắt buộc',
                minLength: { value: 8, message: 'Tối thiểu 8 ký tự' },
              })}
            />
            <button
              type="button"
              id="toggle-password-login"
              className={styles.eyeBtn}
              onClick={() => setShowPassword(!showPassword)}
              tabIndex={-1}
            >
              {showPassword ? '🙈' : '👁️'}
            </button>
          </div>
          {errors.password && <span className="form-error">⚠ {errors.password.message}</span>}
        </div>

        {/* Server error */}
        {serverError && (
          <div className={styles.serverError}>
            <span>⚠</span> {serverError}
          </div>
        )}

        {/* Submit */}
        <button
          id="login-submit"
          type="submit"
          className="btn btn-primary btn-full"
          disabled={loading}
        >
          {loading ? <span className="spinner" /> : 'Đăng nhập'}
          {!loading && <span>→</span>}
        </button>
      </form>

      <div className={styles.footer}>
        Chưa có tài khoản?{' '}
        <Link to="/register" id="go-to-register">Đăng ký nhà hàng ngay</Link>
      </div>
    </AuthLayout>
  )
}
