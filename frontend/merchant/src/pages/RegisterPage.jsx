import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import { register as registerUser } from '../services/authService'
import styles from './AuthPage.module.css'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm()

  const password = watch('password')

  const onSubmit = async (data) => {
    try {
      setLoading(true)
      setServerError('')
      await registerUser({
        email:      data.email,
        password:   data.password,
        first_name: data.first_name,
        last_name:  data.last_name,
        phone:      data.phone,
      })
      navigate('/setup')
    } catch (err) {
      setServerError(err.message || 'Đăng ký thất bại. Vui lòng thử lại.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout>
      <div className={styles.formHeader}>
        <h2 className={styles.formTitle}>Tạo tài khoản mới</h2>
        <p className={styles.formSub}>Bước 1 / 2 — Thông tin cá nhân</p>
      </div>

      <form id="register-form" onSubmit={handleSubmit(onSubmit)} noValidate className={styles.form}>
        {/* Name row */}
        <div className={styles.row2}>
          <div className="form-group">
            <label className="form-label" htmlFor="reg-last-name">Họ</label>
            <input
              id="reg-last-name"
              type="text"
              placeholder="Nguyễn"
              className={`input-field ${errors.last_name ? 'error' : ''}`}
              {...register('last_name', { required: 'Họ là bắt buộc' })}
            />
            {errors.last_name && <span className="form-error">⚠ {errors.last_name.message}</span>}
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="reg-first-name">Tên</label>
            <input
              id="reg-first-name"
              type="text"
              placeholder="An"
              className={`input-field ${errors.first_name ? 'error' : ''}`}
              {...register('first_name', { required: 'Tên là bắt buộc' })}
            />
            {errors.first_name && <span className="form-error">⚠ {errors.first_name.message}</span>}
          </div>
        </div>

        {/* Email */}
        <div className="form-group">
          <label className="form-label" htmlFor="reg-email">Email</label>
          <input
            id="reg-email"
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

        {/* Phone */}
        <div className="form-group">
          <label className="form-label" htmlFor="reg-phone">Số điện thoại</label>
          <input
            id="reg-phone"
            type="tel"
            placeholder="0901234567"
            className={`input-field ${errors.phone ? 'error' : ''}`}
            {...register('phone', {
              required: 'Số điện thoại là bắt buộc',
              pattern: { value: /^0[0-9]{9}$/, message: 'Số điện thoại không hợp lệ (VD: 0901234567)' },
            })}
          />
          {errors.phone && <span className="form-error">⚠ {errors.phone.message}</span>}
        </div>

        {/* Password */}
        <div className="form-group">
          <label className="form-label" htmlFor="reg-password">Mật khẩu</label>
          <div className={styles.inputWrapper}>
            <input
              id="reg-password"
              type={showPassword ? 'text' : 'password'}
              placeholder="Tối thiểu 8 ký tự"
              className={`input-field ${errors.password ? 'error' : ''}`}
              {...register('password', {
                required: 'Mật khẩu là bắt buộc',
                minLength: { value: 8, message: 'Tối thiểu 8 ký tự' },
                pattern: {
                  value: /^(?=.*[A-Z])(?=.*[0-9])/,
                  message: 'Cần có ít nhất 1 chữ hoa và 1 số',
                },
              })}
            />
            <button
              type="button"
              id="toggle-password-reg"
              className={styles.eyeBtn}
              onClick={() => setShowPassword(!showPassword)}
              tabIndex={-1}
            >
              {showPassword ? '🙈' : '👁️'}
            </button>
          </div>
          {errors.password && <span className="form-error">⚠ {errors.password.message}</span>}
        </div>

        {/* Confirm password */}
        <div className="form-group">
          <label className="form-label" htmlFor="reg-confirm">Xác nhận mật khẩu</label>
          <input
            id="reg-confirm"
            type="password"
            placeholder="••••••••"
            className={`input-field ${errors.confirm ? 'error' : ''}`}
            {...register('confirm', {
              required: 'Vui lòng xác nhận mật khẩu',
              validate: (v) => v === password || 'Mật khẩu không khớp',
            })}
          />
          {errors.confirm && <span className="form-error">⚠ {errors.confirm.message}</span>}
        </div>

        {serverError && (
          <div className={styles.serverError}>
            <span>⚠</span> {serverError}
          </div>
        )}

        <button
          id="register-submit"
          type="submit"
          className="btn btn-primary btn-full"
          disabled={loading}
        >
          {loading ? <span className="spinner" /> : 'Tiếp theo'}
          {!loading && <span>→</span>}
        </button>
      </form>

      <div className={styles.footer}>
        Đã có tài khoản?{' '}
        <Link to="/login" id="go-to-login">Đăng nhập ngay</Link>
      </div>
    </AuthLayout>
  )
}
