import { forwardRef, type ReactNode } from 'react'

export type IconInputProps = {
  id: string
  label: string
  requiredMark?: boolean
  icon: ReactNode
  error?: string
} & React.InputHTMLAttributes<HTMLInputElement>

export const IconInput = forwardRef<HTMLInputElement, IconInputProps>(
  function IconInput(
    { id, label, requiredMark, icon, error, className = '', ...props },
    ref,
  ) {
    return (
      <div className="w-full">
        <label
          htmlFor={id}
          className="mb-1.5 block text-sm font-medium text-gray-700"
        >
          {label}
          {requiredMark ? <span className="text-red-600"> *</span> : null}
        </label>
        <div className="relative">
          <span
            className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 [&_svg]:size-5"
            aria-hidden
          >
            {icon}
          </span>
          <input
            ref={ref}
            id={id}
            className={`w-full rounded-lg border border-transparent bg-gray-100 py-2.5 pl-11 pr-3 text-sm text-gray-900 outline-none ring-emerald-800/30 placeholder:text-gray-400 focus:border-emerald-800/20 focus:ring-2 ${className}`}
            {...props}
          />
        </div>
        {error ? (
          <p className="mt-1 text-sm text-red-600" role="alert">
            {error}
          </p>
        ) : null}
      </div>
    )
  },
)
