import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { StatusBadge, PriorityBadge } from '../components/common/StatusBadge'

describe('StatusBadge Component', () => {
  it('renders OPEN status correctly with badge-open class', () => {
    render(<StatusBadge status="OPEN" />)
    const badge = screen.getByText('Open')
    expect(badge).toBeInTheDocument()
    expect(badge.className).toContain('badge-open')
  })

  it('renders IN_PROGRESS status correctly', () => {
    render(<StatusBadge status="IN_PROGRESS" />)
    const badge = screen.getByText('In Progress')
    expect(badge).toBeInTheDocument()
    expect(badge.className).toContain('badge-in-progress')
  })

  it('renders RESOLVED status with badge-resolved class', () => {
    render(<StatusBadge status="RESOLVED" />)
    const badge = screen.getByText('Resolved')
    expect(badge).toBeInTheDocument()
    expect(badge.className).toContain('badge-resolved')
  })

  it('renders WAITING_FOR_CUSTOMER status correctly', () => {
    render(<StatusBadge status="WAITING_FOR_CUSTOMER" />)
    const badge = screen.getByText('Waiting on Customer')
    expect(badge).toBeInTheDocument()
    expect(badge.className).toContain('badge-waiting')
  })
})

describe('PriorityBadge Component', () => {
  it('renders CRITICAL priority with prominent badge-critical styling', () => {
    render(<PriorityBadge priority="CRITICAL" />)
    const badge = screen.getByText('Critical')
    expect(badge).toBeInTheDocument()
    expect(badge.className).toContain('badge-critical')
  })

  it('renders HIGH priority correctly', () => {
    render(<PriorityBadge priority="HIGH" />)
    const badge = screen.getByText('High')
    expect(badge).toBeInTheDocument()
    expect(badge.className).toContain('badge-high')
  })

  it('renders LOW priority correctly', () => {
    render(<PriorityBadge priority="LOW" />)
    const badge = screen.getByText('Low')
    expect(badge).toBeInTheDocument()
    expect(badge.className).toContain('badge-low')
  })
})
