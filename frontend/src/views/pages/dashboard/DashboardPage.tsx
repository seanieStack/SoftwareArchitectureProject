import { useAppDispatch, useAppSelector } from '../../../redux-features/store/hooks'
import { logout } from '../../../redux-features/slices/user/userSlice'
import { PrimaryButton } from '../../components/buttons/PrimaryButton'

export function DashboardPage() {
  const dispatch = useAppDispatch()
  const profile = useAppSelector((s) => s.user.profile)

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-4xl items-center justify-between px-4 py-4 md:px-6">
          <h1 className="text-lg font-semibold text-gray-900">E-Library</h1>
          <PrimaryButton
            type="button"
            className="w-auto px-5"
            onClick={() => {
              dispatch(logout())
            }}
          >
            Sign out
          </PrimaryButton>
        </div>
      </header>
      <main className="mx-auto max-w-4xl px-4 py-10 md:px-6">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">
          Dashboard
        </h2>
        <p className="mt-2 text-gray-600">
          You are signed in as{' '}
          <span className="font-medium text-gray-900">{profile?.email}</span>
          {profile ? (
            <span className="text-gray-600">
              {' '}
              ({profile.userType})
            </span>
          ) : null}
          .
        </p>
      </main>
    </div>
  )
}
