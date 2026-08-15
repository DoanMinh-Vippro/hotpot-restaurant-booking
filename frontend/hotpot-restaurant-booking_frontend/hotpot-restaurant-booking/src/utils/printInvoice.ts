import type { HoaDon, HoaDonChiTiet } from '@/api/HoaDonApi'

const formatCurrency = (value: number | string | null) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(Number(value ?? 0))

const formatDateTime = (value: string | number[] | null) => {
  if (!value) return 'Chưa xuất'

  let date: Date
  if (Array.isArray(value)) {
    const [year = 0, month = 1, day = 1, hour = 0, minute = 0, second = 0] = value
    date = new Date(year, month - 1, day, hour, minute, second)
  } else {
    date = new Date(value)
  }

  if (Number.isNaN(date.getTime())) return String(value)

  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

const invoiceStatusLabel = (status: number | null | undefined) => {
  if (status === 1) return 'Đã xuất'
  if (status === 0) return 'Nháp'
  return 'Không rõ'
}

const paymentStatusLabel = (status: number | null | undefined) => {
  if (status === 1) return 'Đã thanh toán'
  if (status === 0) return 'Chưa thanh toán'
  return 'Không rõ'
}

const paymentMethodLabel = (method: number | null | undefined) => {
  if (method === 1) return 'Tiền mặt'
  if (method === 2) return 'Chuyển khoản'
  if (method === 3) return 'Thẻ'
  return 'Chưa có'
}

const escapeHtml = (value: string | number | null | undefined) =>
  String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const itemName = (item: HoaDonChiTiet) => item.tenMon ?? item.tenCombo ?? 'Món chưa đặt tên'

const formatInvoiceTableLabel = (invoice: HoaDon) => {
  const names: string[] = []

  if (Array.isArray((invoice as any)?.dsBan)) {
    ;(invoice as any).dsBan.forEach((ban: any) => {
      const name = String(ban?.tenBan || ban?.name || ban?.ten || '').trim()
      if (name && !names.includes(name)) names.push(name)
    })
  }

  if (names.length === 0) {
    const raw = String((invoice as any)?.tenBan || '').trim()
    if (raw) {
      const splitNames = raw.split(/[;,]/).map((item: string) => item.trim()).filter(Boolean)
      splitNames.forEach((name: string) => {
        if (name && !names.includes(name)) names.push(name)
      })
    }
  }

  if (names.length > 0) return `${names.join(', ')} (${names.length} bàn)`
  return invoice.loaiBan || `Bàn ${invoice.idBan ?? '-'}`
}

export const printInvoiceReceipt = (invoice: HoaDon | null | undefined, items: HoaDonChiTiet[] = []) => {
  if (!invoice) return

  const printWindow = window.open('', '_blank', 'width=900,height=1200')
  if (!printWindow) {
    window.alert('Trình duyệt đã chặn cửa sổ in. Vui lòng cho phép popup để xuất PDF.')
    return
  }

  const rows = items.length
    ? items
        .map((item) => {
          const comboText = item.comboItems?.length ? `<div class="combo-items">Gồm: ${escapeHtml(item.comboItems.join(', '))}</div>` : ''
          return `
            <tr>
              <td>${escapeHtml(item.maHoaDonChiTiet)}</td>
              <td>
                <div>${escapeHtml(itemName(item))}</div>
                ${comboText}
              </td>
              <td>${escapeHtml(item.soLuong ?? 0)}</td>
              <td>${escapeHtml(formatCurrency(item.giaBanTaiThoiDiem))}</td>
              <td>${escapeHtml(formatCurrency(item.tienGiamGiaMon))}</td>
              <td>${escapeHtml(formatCurrency(item.thanhTien))}</td>
            </tr>
          `
        })
        .join('')
    : '<tr><td colspan="6">Hóa đơn này chưa có chi tiết món.</td></tr>'

  const html = `<!DOCTYPE html>
  <html lang="vi">
    <head>
      <meta charset="UTF-8" />
      <title>${escapeHtml(invoice.maHoaDon || 'Hóa đơn')}</title>
      <style>
        :root { color-scheme: light only; }
        * { box-sizing: border-box; }
        body {
          font-family: 'Segoe UI', Arial, sans-serif;
          color: #1f2937;
          margin: 0;
          padding: 24px;
          background: #f7f5ef;
        }
        .invoice-box {
          max-width: 920px;
          margin: 0 auto;
          border: 1px solid #e5d8b8;
          padding: 28px;
          border-radius: 16px;
          background: #fffdf8;
          box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
        }
        .header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 20px;
          margin-bottom: 22px;
          border-bottom: 2px solid #d7b46a;
          padding-bottom: 16px;
        }
        .brand {
          font-size: 28px;
          font-weight: 800;
          color: #8c5f14;
          margin: 0 0 6px;
        }
        .title { font-size: 22px; font-weight: 700; margin: 0 0 6px; color: #111827; }
        .sub { color: #6b7280; margin: 4px 0; }
        .status-badge {
          display: inline-block;
          padding: 5px 10px;
          border-radius: 999px;
          background: #fff4d8;
          color: #8c5f14;
          font-size: 12px;
          font-weight: 700;
          margin-bottom: 8px;
        }
        .total-box {
          background: linear-gradient(135deg, #d7b46a, #c89b3a);
          color: #1f1407;
          padding: 16px 18px;
          border-radius: 12px;
          min-width: 200px;
          text-align: right;
          box-shadow: 0 6px 16px rgba(199, 155, 58, 0.22);
        }
        .grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; margin-bottom: 16px; }
        .card {
          border: 1px solid #efe3bf;
          border-radius: 10px;
          padding: 12px 14px;
          background: #fff;
        }
        .label { display: block; font-size: 11px; color: #8a7b57; margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.04em; }
        .value { font-weight: 600; color: #111827; }
        table { width: 100%; border-collapse: collapse; margin-top: 8px; }
        th, td { border-bottom: 1px solid #eee3c7; padding: 10px 8px; text-align: left; }
        th { background: #f8efe0; color: #8c5f14; font-size: 12px; text-transform: uppercase; }
        tbody tr:nth-child(even) { background: #fcfaf6; }
        .combo-items { color: #6b7280; font-size: 12px; margin-top: 4px; }
        .summary { margin-top: 16px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
        .summary .card { background: #fffcf4; }
        .footer { margin-top: 24px; color: #6b7280; font-size: 12px; text-align: center; border-top: 1px dashed #e5d8b8; padding-top: 12px; }
        .divider { height: 1px; background: linear-gradient(90deg, transparent, #e5d8b8, transparent); margin: 12px 0 18px; }
        @media print {
          body { padding: 0; background: #fff; }
          .invoice-box { border: none; box-shadow: none; margin: 0; border-radius: 0; }
        }
      </style>
    </head>
    <body>
      <div class="invoice-box">
        <div class="header">
          <div>
            <div class="status-badge">${escapeHtml(invoiceStatusLabel(invoice.trangThaiHoaDon))}</div>
            <div class="brand">Hotpot Restaurant</div>
            <h1 class="title">${escapeHtml(invoice.maHoaDon || 'HÓA ĐƠN')}</h1>
            <p class="sub">Ngày xuất: ${escapeHtml(formatDateTime(invoice.thoiGianXuat))}</p>
            <p class="sub">Mã giao dịch: ${escapeHtml(invoice.maGiaoDich || 'Không có')}</p>
          </div>
          <div class="total-box">
            <div>Tổng tiền</div>
            <div style="font-size: 24px; font-weight: 800; margin-top: 6px;">${escapeHtml(formatCurrency(invoice.tongTien))}</div>
          </div>
        </div>

        <div class="divider"></div>

        <div class="grid">
          <div class="card">
            <span class="label">Khách hàng</span>
            <div class="value">${escapeHtml(invoice.tenKhachHang || 'Khách lẻ')}</div>
          </div>
          <div class="card">
            <span class="label">Số điện thoại</span>
            <div class="value">${escapeHtml(invoice.sdtKhachHang || 'Chưa có')}</div>
          </div>
          <div class="card">
            <span class="label">Bàn</span>
            <div class="value">${escapeHtml(formatInvoiceTableLabel(invoice))}</div>
          </div>
          <div class="card">
            <span class="label">Nhân viên</span>
            <div class="value">${escapeHtml(invoice.tenNhanVien || 'Chưa có')}</div>
          </div>
          <div class="card">
            <span class="label">Giờ vào bàn</span>
            <div class="value">${escapeHtml(formatDateTime((invoice as any).gioVaoBan || invoice.thoiGianXuat))}</div>
          </div>
          <div class="card">
            <span class="label">Giờ ra bàn</span>
            <div class="value">${escapeHtml(formatDateTime((invoice as any).gioRoiBan || (invoice.trangThaiThanhToan === 1 ? invoice.thoiGianXuat : null)))}</div>
          </div>
          <div class="card">
            <span class="label">Thanh toán</span>
            <div class="value">${escapeHtml(paymentStatusLabel(invoice.trangThaiThanhToan))}</div>
          </div>
          <div class="card">
            <span class="label">Phương thức</span>
            <div class="value">${escapeHtml(paymentMethodLabel(invoice.phuongThucThanhToan))}</div>
          </div>
        </div>

        <div class="summary">
          <div class="card">
            <span class="label">Trước giảm</span>
            <div class="value">${escapeHtml(formatCurrency(invoice.tienTruocGiam))}</div>
          </div>
          <div class="card">
            <span class="label">Tiền cọc</span>
            <div class="value">${escapeHtml(formatCurrency(invoice.tienCoc))}</div>
          </div>
          <div class="card">
            <span class="label">Giảm giá</span>
            <div class="value">${escapeHtml(formatCurrency(invoice.tienGiamGia))}${invoice.maGiamGia ? ` (${escapeHtml(invoice.maGiamGia)})` : ''}</div>
          </div>
        </div>

        <table>
          <thead>
            <tr>
              <th>Mã</th>
              <th>Món / combo</th>
              <th>SL</th>
              <th>Đơn giá</th>
              <th>Giảm</th>
              <th>Thành tiền</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>

        <div class="footer">
          <div>Được xuất từ hệ thống đặt bàn Hotpot</div>
          <div>Cảm ơn quý khách và hẹn gặp lại!</div>
        </div>
      </div>
    </body>
  </html>`

  printWindow.document.write(html)
  printWindow.document.close()
  printWindow.focus()

  setTimeout(() => {
    printWindow.print()
  }, 300)
}
