import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import StepIndicator from '../components/StepIndicator'
import OperatingHoursTable, { defaultOperatingHours } from '../components/OperatingHoursTable'
import { registerRestaurant } from '../services/merchantService'
import styles from './SetupRestaurantPage.module.css'

const STEPS = ['Thông tin cơ bản', 'Địa chỉ', 'Giờ hoạt động']

export default function SetupRestaurantPage() {
  const navigate = useNavigate()
  const [currentStep, setCurrentStep] = useState(0)
  const [loading, setLoading] = useState(false)
  const [serverError, setServerError] = useState('')
  const [operatingHours, setOperatingHours] = useState(defaultOperatingHours)

  // Collected data across steps
  const [formData, setFormData] = useState({})

  const { register, handleSubmit, formState: { errors }, trigger } = useForm({ mode: 'onChange' })

  // Step 0 fields
  const step0Fields = ['name', 'description']
  // Step 1 fields
  const step1Fields = ['address', 'latitude', 'longitude']

  async function handleNext() {
    const fieldsToValidate = currentStep === 0 ? step0Fields : step1Fields
    const valid = await trigger(fieldsToValidate)
    if (!valid) return

    // Collect values via form submit won't help here — read from RHF state
    const allValues = document.querySelector('#setup-form')
    const fd = new FormData(allValues)
    const collected = Object.fromEntries(fd.entries())

    setFormData((prev) => ({ ...prev, ...collected }))
    setCurrentStep((s) => s + 1)
  }

  async function handleFinalSubmit(formValues) {
    try {
      setLoading(true)
      setServerError('')

      const payload = {
        name:        formValues.name,
        description: formValues.description || '',
        address:     formValues.address,
        latitude:    parseFloat(formValues.latitude),
        longitude:   parseFloat(formValues.longitude),
        operating_hours: operatingHours,
      }

      await registerRestaurant(payload)
      navigate('/pending')
    } catch (err) {
      setServerError(err.message || 'Đăng ký thất bại. Vui lòng thử lại.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.logo}>
          <svg width="32" height="32" viewBox="0 0 40 40" fill="none">
            <rect width="40" height="40" rx="12" fill="url(#g2)"/>
            <path d="M10 28 Q12 15 20 14 Q28 15 30 28" stroke="white" strokeWidth="2.5" strokeLinecap="round" fill="none"/>
            <circle cx="20" cy="11" r="3" fill="white"/>
            <path d="M14 28 h12" stroke="white" strokeWidth="2.5" strokeLinecap="round"/>
            <defs>
              <linearGradient id="g2" x1="0" y1="0" x2="40" y2="40">
                <stop offset="0%" stopColor="#ff6b35"/>
                <stop offset="100%" stopColor="#f7931e"/>
              </linearGradient>
            </defs>
          </svg>
          <span className={styles.logoText}>FoodDelivery</span>
        </div>
        <div className={styles.headerRight}>
          <span className={styles.stepBadge}>Bước 2 / 2 — Đăng ký nhà hàng</span>
        </div>
      </div>

      {/* Content */}
      <div className={styles.content}>
        <div className={styles.card}>
          <div className={styles.cardHeader}>
            <h1 className={styles.title}>Thiết lập nhà hàng</h1>
            <p className={styles.sub}>Hoàn tất đăng ký để bắt đầu nhận đơn hàng</p>
          </div>

          <StepIndicator steps={STEPS} currentStep={currentStep} />

          <form id="setup-form" onSubmit={handleSubmit(handleFinalSubmit)} noValidate>
            {/* ── Step 0: Basic Info ────────────────────────────── */}
            {currentStep === 0 && (
              <div className={styles.stepContent}>
                <div className="form-group">
                  <label className="form-label" htmlFor="r-name">Tên nhà hàng *</label>
                  <input
                    id="r-name"
                    name="name"
                    type="text"
                    placeholder="VD: Phở Bắc Hà"
                    className={`input-field ${errors.name ? 'error' : ''}`}
                    {...register('name', {
                      required: 'Tên nhà hàng là bắt buộc',
                      maxLength: { value: 255, message: 'Tối đa 255 ký tự' },
                    })}
                  />
                  {errors.name && <span className="form-error">⚠ {errors.name.message}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="r-desc">Mô tả (tuỳ chọn)</label>
                  <textarea
                    id="r-desc"
                    name="description"
                    placeholder="Giới thiệu ngắn về nhà hàng của bạn..."
                    className="input-field"
                    {...register('description')}
                  />
                </div>

                <div className={styles.infoBox}>
                  <span className={styles.infoIcon}>💡</span>
                  <span>Tên và mô tả nhà hàng sẽ hiển thị cho khách hàng tìm kiếm. Hãy mô tả đặc trưng ẩm thực của bạn!</span>
                </div>
              </div>
            )}

            {/* ── Step 1: Address ──────────────────────────────── */}
            {currentStep === 1 && (
              <div className={styles.stepContent}>
                <div className="form-group">
                  <label className="form-label" htmlFor="r-address">Địa chỉ đầy đủ *</label>
                  <input
                    id="r-address"
                    name="address"
                    type="text"
                    placeholder="VD: 45 Nguyễn Trãi, Quận 5, TP.HCM"
                    className={`input-field ${errors.address ? 'error' : ''}`}
                    {...register('address', { required: 'Địa chỉ là bắt buộc' })}
                  />
                  {errors.address && <span className="form-error">⚠ {errors.address.message}</span>}
                </div>

                <div className={styles.coordRow}>
                  <div className="form-group">
                    <label className="form-label" htmlFor="r-lat">Vĩ độ (Latitude) *</label>
                    <input
                      id="r-lat"
                      name="latitude"
                      type="number"
                      step="0.000001"
                      placeholder="10.758000"
                      className={`input-field ${errors.latitude ? 'error' : ''}`}
                      {...register('latitude', {
                        required: 'Vĩ độ là bắt buộc',
                        min: { value: -90, message: 'Không hợp lệ' },
                        max: { value: 90, message: 'Không hợp lệ' },
                      })}
                    />
                    {errors.latitude && <span className="form-error">⚠ {errors.latitude.message}</span>}
                  </div>
                  <div className="form-group">
                    <label className="form-label" htmlFor="r-lng">Kinh độ (Longitude) *</label>
                    <input
                      id="r-lng"
                      name="longitude"
                      type="number"
                      step="0.000001"
                      placeholder="106.682000"
                      className={`input-field ${errors.longitude ? 'error' : ''}`}
                      {...register('longitude', {
                        required: 'Kinh độ là bắt buộc',
                        min: { value: -180, message: 'Không hợp lệ' },
                        max: { value: 180, message: 'Không hợp lệ' },
                      })}
                    />
                    {errors.longitude && <span className="form-error">⚠ {errors.longitude.message}</span>}
                  </div>
                </div>

                <div className={styles.infoBox}>
                  <span className={styles.infoIcon}>📍</span>
                  <span>Toạ độ được dùng để tính khoảng cách giao hàng. Bạn có thể lấy toạ độ từ Google Maps.</span>
                </div>
              </div>
            )}

            {/* ── Step 2: Operating Hours ──────────────────────── */}
            {currentStep === 2 && (
              <div className={styles.stepContent}>
                <p className={styles.hoursDesc}>
                  Cấu hình giờ hoạt động mỗi ngày trong tuần. Bật <strong>Nghỉ</strong> để đánh dấu ngày nghỉ.
                </p>
                <OperatingHoursTable
                  value={operatingHours}
                  onChange={setOperatingHours}
                />
              </div>
            )}

            {/* ── Error ─────────────────────────────────────────── */}
            {serverError && (
              <div className={styles.serverError}>
                <span>⚠</span> {serverError}
              </div>
            )}

            {/* ── Navigation buttons ───────────────────────────── */}
            <div className={styles.btnRow}>
              {currentStep > 0 && (
                <button
                  type="button"
                  id="setup-back"
                  className="btn btn-ghost"
                  onClick={() => setCurrentStep((s) => s - 1)}
                >
                  ← Quay lại
                </button>
              )}

              {currentStep < STEPS.length - 1 ? (
                <button
                  type="button"
                  id="setup-next"
                  className="btn btn-primary"
                  style={{ marginLeft: 'auto' }}
                  onClick={handleNext}
                >
                  Tiếp theo →
                </button>
              ) : (
                <button
                  type="submit"
                  id="setup-submit"
                  className="btn btn-primary"
                  style={{ marginLeft: 'auto' }}
                  disabled={loading}
                >
                  {loading ? <span className="spinner" /> : '🚀 Gửi đăng ký'}
                </button>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
