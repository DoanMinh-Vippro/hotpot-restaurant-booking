export interface DatBanQuanLyForm {
  idKhachHang: number | null
  tenKhachHang: string
  sdtKhachHang: string
  soNguoi: number
  thoiGianDenDuKien: string
  dsBan: number[]
  dsCombo: any[]
  soTienCoc: number
  trangThaiCoc: string
  phuongThucThanhToan: string
  ghiChu: string
  trangThai: string
}

interface Ban {
  idBan: number
  sucChua: number
}

export interface ValidateError {
  field: keyof DatBanQuanLyForm | 'general'
  message: string
}

function validateThoiGianHoatDong(thoiGian: string): ValidateError | null {
  if (!thoiGian) {
    return {
      field: 'thoiGianDenDuKien',
      message: 'Vui lòng chọn thời gian đến',
    }
  }

  const date = new Date(thoiGian)
  const hour = date.getHours()

  const caTrua = hour >= 10 && hour < 14
  const caToi = hour >= 18

  if (!caTrua && !caToi) {
    return {
      field: 'thoiGianDenDuKien',
      message: 'Nhà hàng chỉ nhận đặt bàn từ 10:00 - 14:00 và sau 18:00.',
    }
  }

  return null
}

function validateSucChuaBan(
  dsBanDaChon: number[],
  dsBanTrong: Ban[],
  soNguoi: number,
): ValidateError | null {
  const tongSucChua = dsBanTrong
    .filter((b) => dsBanDaChon.includes(b.idBan))
    .reduce((tong, b) => tong + b.sucChua, 0)

  if (tongSucChua < soNguoi) {
    return {
      field: 'dsBan',
      message: 'Tổng sức chứa của các bàn được chọn không đủ số người.',
    }
  }

  return null
}

export function validateDatBanQuanLy(
  form: DatBanQuanLyForm,
  dsBanTrong: Ban[],
): ValidateError | null {
  if (!form.sdtKhachHang.trim()) {
    return {
      field: 'sdtKhachHang',
      message: 'Vui lòng nhập số điện thoại',
    }
  }

  if (!/^(0[35789])[0-9]{8}$/.test(form.sdtKhachHang)) {
    return {
      field: 'sdtKhachHang',
      message: 'Số điện thoại không hợp lệ',
    }
  }

  if (form.tenKhachHang.trim().length === 0) {
    return {
      field: 'tenKhachHang',
      message: 'Vui lòng nhập tên khách hàng',
    }
  }

  if (form.tenKhachHang.length > 100) {
    return {
      field: 'tenKhachHang',
      message: 'Tên khách hàng tối đa 100 ký tự',
    }
  }

  if (form.soNguoi < 1) {
    return {
      field: 'soNguoi',
      message: 'Số người phải lớn hơn 0',
    }
  }

  if (form.ghiChu.length > 500) {
    return {
      field: 'ghiChu',
      message: 'Ghi chú tối đa 500 ký tự',
    }
  }

  if (form.soTienCoc < 0) {
    return {
      field: 'soTienCoc',
      message: 'Tiền cọc không hợp lệ',
    }
  }

  if (form.dsBan.length === 0) {
    return {
      field: 'dsBan',
      message: 'Vui lòng chọn ít nhất một bàn',
    }
  }

  const loiThoiGian = validateThoiGianHoatDong(form.thoiGianDenDuKien)
  if (loiThoiGian) {
    return loiThoiGian
  }

  const loiSucChua = validateSucChuaBan(form.dsBan, dsBanTrong, form.soNguoi)

  if (loiSucChua) {
    return loiSucChua
  }

  return null
}
