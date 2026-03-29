/**
 * Định dạng tiền tệ VND
 * @param {number} amount - Số tiền
 * @returns {string} Chuỗi định dạng tiền tệ
 */
export const formatCurrency = (amount) => {
  if (typeof amount !== 'number') return '0 đ';
  
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
};

/**
 * Hàm format ngày giờ m/d/y
 */
export const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN');
};
