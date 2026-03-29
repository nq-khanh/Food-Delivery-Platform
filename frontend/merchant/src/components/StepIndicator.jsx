import styles from './StepIndicator.module.css'

/**
 * StepIndicator — progress indicator for multi-step forms
 * @param {{ steps: string[], currentStep: number }} props
 * currentStep is 0-indexed
 */
export default function StepIndicator({ steps, currentStep }) {
  return (
    <div className={styles.wrapper}>
      {steps.map((label, idx) => {
        const done    = idx < currentStep
        const active  = idx === currentStep
        return (
          <div key={idx} className={styles.step}>
            <div className={styles.stepLine}>
              {/* Connector before */}
              {idx > 0 && (
                <div className={`${styles.connector} ${done || active ? styles.connectorDone : ''}`} />
              )}
              {/* Circle */}
              <div
                className={`${styles.circle} ${active ? styles.circleActive : ''} ${done ? styles.circleDone : ''}`}
              >
                {done ? (
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M2.5 7L5.5 10L11.5 4" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                ) : (
                  <span>{idx + 1}</span>
                )}
              </div>
            </div>
            <span className={`${styles.label} ${active ? styles.labelActive : ''} ${done ? styles.labelDone : ''}`}>
              {label}
            </span>
          </div>
        )
      })}
    </div>
  )
}
