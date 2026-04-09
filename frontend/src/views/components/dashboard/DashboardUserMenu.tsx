import * as DropdownMenu from '@radix-ui/react-dropdown-menu'
import { Link } from 'react-router-dom'
import { APP_ROUTES } from '../../../constants/routes'

type DashboardUserMenuProps = {
  displayName: string
  onLogout: () => void
}

const itemClass =
  'relative flex cursor-pointer select-none items-center rounded-lg px-3 py-2 text-sm text-gray-900 outline-none data-[disabled]:pointer-events-none data-[highlighted]:bg-gray-100 data-[disabled]:opacity-50'

function ChevronDownIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M6 9l6 6 6-6"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

export function DashboardUserMenu({ displayName, onLogout }: DashboardUserMenuProps) {
  const initial = displayName.slice(0, 1).toUpperCase()

  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger asChild>
        <button
          type="button"
          className="inline-flex max-w-full shrink-0 items-center gap-2 rounded-xl border border-transparent px-2 py-1.5 text-left outline-none transition hover:border-gray-200/80 hover:bg-gray-50 focus-visible:border-emerald-300 focus-visible:ring-2 focus-visible:ring-emerald-200 data-[state=open]:border-gray-200 data-[state=open]:bg-gray-50 sm:gap-3 sm:px-2.5 sm:py-2"
          aria-label="Account menu"
        >
          <span className="hidden min-w-0 text-right sm:block">
            <span className="block max-w-[12rem] truncate text-sm font-semibold text-gray-900">
              {displayName}
            </span>
            <span className="block text-xs text-gray-500">Student</span>
          </span>
          <span
            className="flex size-9 shrink-0 items-center justify-center rounded-full bg-emerald-100 text-sm font-semibold text-emerald-900 sm:size-10"
            aria-hidden
          >
            {initial}
          </span>
          <ChevronDownIcon className="size-4 shrink-0 text-gray-500 sm:size-5" />
        </button>
      </DropdownMenu.Trigger>

      <DropdownMenu.Portal>
        <DropdownMenu.Content
          className="z-50 min-w-[11rem] overflow-hidden rounded-xl border border-gray-200 bg-white p-1 shadow-lg will-change-[opacity,transform]"
          sideOffset={8}
          align="end"
        >
          <DropdownMenu.Item asChild className={itemClass}>
            <Link to={APP_ROUTES.STUDENT_PROFILE}>User profile</Link>
          </DropdownMenu.Item>
          <DropdownMenu.Separator className="my-1 h-px bg-gray-200" />
          <DropdownMenu.Item className={itemClass} onSelect={onLogout}>
            Log out
          </DropdownMenu.Item>
        </DropdownMenu.Content>
      </DropdownMenu.Portal>
    </DropdownMenu.Root>
  )
}
