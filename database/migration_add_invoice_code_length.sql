-- SQL Server migration for split invoices.
-- Safe to run once on an existing database.

IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.HoaDon')
      AND name = N'ma_hoa_don'
)
BEGIN
    ALTER TABLE [dbo].[HoaDon]
    ALTER COLUMN [ma_hoa_don] VARCHAR(50) NULL;
END;
GO

-- The split flow also generates detail codes. Keep this column compatible
-- with the existing short code format.
IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.HoaDonChiTiet')
      AND name = N'ma_hoa_don_chi_tiet'
)
BEGIN
    ALTER TABLE [dbo].[HoaDonChiTiet]
    ALTER COLUMN [ma_hoa_don_chi_tiet] VARCHAR(20) NULL;
END;
GO
