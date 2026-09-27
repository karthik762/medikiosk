import React from 'react';

interface KioskButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'outline' | 'success';
  size?: 'md' | 'lg' | 'xl';
  loading?: boolean;
  icon?: React.ReactNode;
}

export const KioskButton: React.FC<KioskButtonProps> = ({
  children,
  variant = 'primary',
  size = 'lg',
  loading = false,
  icon,
  className = '',
  disabled,
  ...props
}) => {
  const baseStyles = 'font-semibold rounded-2xl transition-all duration-200 flex items-center justify-center gap-3 active:scale-[0.98] shadow-sm focus:outline-none focus:ring-4 select-none';

  const sizeStyles = {
    md: 'py-3 px-6 text-base min-h-[48px]',
    lg: 'py-4 px-8 text-lg min-h-[56px]',
    xl: 'py-5 px-10 text-xl min-h-[64px]',
  }[size];

  const variantStyles = {
    primary: 'bg-[#AEA989] hover:bg-[#b8b394] text-[#131936] font-bold shadow-md focus:ring-[#AEA989]/50 disabled:bg-[#AEA989]/40',
    secondary: 'bg-[#131936] hover:bg-[#1c244b] text-[#AEA989] border border-[#AEA989]/30 focus:ring-[#AEA989]/30 disabled:bg-[#131936]/50 disabled:text-slate-500',
    outline: 'border-2 border-[#AEA989] text-[#AEA989] bg-[#131936] hover:bg-[#AEA989] hover:text-[#131936] font-bold focus:ring-[#AEA989]/30 disabled:border-slate-700 disabled:text-slate-500',
    danger: 'bg-rose-600 hover:bg-rose-700 text-white shadow-rose-900/40 focus:ring-rose-400 disabled:bg-rose-950',
    success: 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-emerald-900/40 focus:ring-emerald-400 disabled:bg-emerald-950',
  }[variant];

  return (
    <button
      className={`${baseStyles} ${sizeStyles} ${variantStyles} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <div className="w-6 h-6 border-3 border-current border-t-transparent rounded-full animate-spin" />
      ) : (
        <>
          {icon}
          {children}
        </>
      )}
    </button>
  );
};
