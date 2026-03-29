import { useState } from 'react'
import styles from './OperatingHoursTable.module.css'

const DAYS = [
  { key: 0, label: 'Chủ nhật',  short: 'CN' },
  { key: 1, label: 'Thứ Hai',   short: 'T2' },
  { key: 2, label: 'Thứ Ba',    short: 'T3' },
  { key: 3, label: 'Thứ Tư',    short: 'T4' },
  { key: 4, label: 'Thứ Năm',   short: 'T5' },
  { key: 5, label: 'Thứ Sáu',   short: 'T6' },
  { key: 6, label: 'Thứ Bảy',   short: 'T7' },
]

const DEFAULT_OPEN  = '07:00'
const DEFAULT_CLOSE = '22:00'

/**
 * OperatingHoursTable
 * Manages weekly operating schedule — matches API payload:
 * [{ day_of_week, open_time, close_time, is_closed }]
 *
 * @param {{ value: array, onChange: fn }} props
 */
export default function OperatingHoursTable({ value, onChange }) {
  function toggleClosed(dayKey) {
    onChange(
      value.map((row) =>
        row.day_of_week === dayKey
          ? { ...row, is_closed: !row.is_closed, open_time: null, close_time: null }
          : row
      )
    )
  }

  function setTime(dayKey, field, time) {
    onChange(
      value.map((row) =>
        row.day_of_week === dayKey ? { ...row, [field]: time } : row
      )
    )
  }

  return (
    <div className={styles.table}>
      <div className={styles.header}>
        <span>Ngày</span>
        <span>Mở cửa</span>
        <span>Đóng cửa</span>
        <span>Nghỉ</span>
      </div>
      {DAYS.map(({ key, label }) => {
        const row = value.find((r) => r.day_of_week === key) || {
          day_of_week: key,
          open_time: DEFAULT_OPEN,
          close_time: DEFAULT_CLOSE,
          is_closed: false,
        }
        return (
          <div key={key} className={`${styles.row} ${row.is_closed ? styles.rowClosed : ''}`}>
            <span className={styles.dayName}>{label}</span>

            <input
              type="time"
              className={styles.timeInput}
              value={row.open_time || DEFAULT_OPEN}
              disabled={row.is_closed}
              onChange={(e) => setTime(key, 'open_time', e.target.value)}
            />

            <input
              type="time"
              className={styles.timeInput}
              value={row.close_time || DEFAULT_CLOSE}
              disabled={row.is_closed}
              onChange={(e) => setTime(key, 'close_time', e.target.value)}
            />

            <label className={styles.toggle}>
              <input
                type="checkbox"
                checked={row.is_closed}
                onChange={() => toggleClosed(key)}
              />
              <span className={styles.toggleSlider} />
            </label>
          </div>
        )
      })}
    </div>
  )
}

/** Default initial value — all days open 07:00–22:00 */
export function defaultOperatingHours() {
  return [0, 1, 2, 3, 4, 5, 6].map((day) => ({
    day_of_week: day,
    open_time:   DEFAULT_OPEN,
    close_time:  DEFAULT_CLOSE,
    is_closed:   day === 0, // Chủ nhật nghỉ mặc định
  }))
}
