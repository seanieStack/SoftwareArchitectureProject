import { forwardRef } from 'react'
import { ChevronDownIcon } from './authIcons'

const OPTIONS = [
  { value: 'student', label: 'Student' },
  { value: 'staff', label: 'Staff' },
  { value: 'admin', label: 'Admin' },
] as const

export type UserTypeSelectProps = {
  id: string
  label: string
  error?: string
} & React.SelectHTMLAttributes<HTMLSelectElement>

export const UserTypeSelect = forwardRef<HTMLSelectElement, UserTypeSelectProps>(
  function UserTypeSelect({ id, label, error, className = '', ...props }, ref) {
    return (
      <div className="w-full">
        <label
          htmlFor={id}
          className="mb-1.5 block text-sm font-medium text-gray-700"
        >
          {label}
          <span className="text-red-600"> *</span>
        </label>
        <div className="relative">
          <select
            ref={ref}
            id={id}
            className={`w-full appearance-none rounded-lg border border-transparent bg-gray-100 py-2.5 pl-3 pr-10 text-sm text-gray-900 outline-none ring-emerald-800/30 focus:border-emerald-800/20 focus:ring-2 ${className}`}
            {...props}
          >
            {OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
          <ChevronDownIcon className="pointer-events-none absolute right-3 top-1/2 size-5 -translate-y-1/2 text-gray-400" />
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
