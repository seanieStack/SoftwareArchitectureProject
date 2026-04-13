import { WEBSITE_LOGO_SRC } from '../../../constants/assets'

type AuthHeaderProps = {
  title: string
  subtitle: string
  /** When true, title and subtitle are centered (e.g. register). */
  align?: 'start' | 'center'
}

export function AuthHeader({ title, subtitle, align = 'start' }: AuthHeaderProps) {
  const alignClass = align === 'center' ? 'text-center' : 'text-left'
  const logoAlignClass = align === 'center' ? 'mx-auto' : ''

  return (
    <header className={`mb-6 ${alignClass}`}>
      <img
        src={WEBSITE_LOGO_SRC}
        alt="UL E-Library"
        className={`mb-3 block h-auto max-h-[7.25rem] w-full max-w-[20rem] object-contain object-top md:max-h-[8.25rem] md:max-w-[24rem] ${logoAlignClass}`}
        width={320}
        height={280}
        decoding="async"
        fetchPriority="high"
      />
      <div className="space-y-1.5">
        <h1 className="text-2xl font-bold tracking-tight text-gray-900 md:text-3xl">
          {title}
        </h1>
        <p className="text-sm text-gray-500 md:text-base">{subtitle}</p>
      </div>
    </header>
  )
}
