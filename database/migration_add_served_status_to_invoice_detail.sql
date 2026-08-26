-- Add persisted kitchen/service status for invoice detail rows.
-- SQL Server; run once against the application database.

IF COL_LENGTH('dbo.HoaDonChiTiet', 'da_len') IS NULL
BEGIN
    ALTER TABLE [dbo].[HoaDonChiTiet]
    ADD [da_len] INT NOT NULL CONSTRAINT [DF_HoaDonChiTiet_da_len] DEFAULT (0);
END;
GO

IF COL_LENGTH('dbo.HoaDonChiTiet', 'trang_thai_mon_an') IS NULL
BEGIN
    ALTER TABLE [dbo].[HoaDonChiTiet]
    ADD [trang_thai_mon_an] VARCHAR(30) NULL;
END;
GO

UPDATE [dbo].[HoaDonChiTiet]
SET [trang_thai_mon_an] = CASE
    WHEN [da_len] >= [so_luong] AND [so_luong] > 0 THEN 'DA_LEN'
    ELSE 'DANG_LEN'
END
WHERE [trang_thai_mon_an] IS NULL;
GO
