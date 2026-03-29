import { useEffect } from 'react';

/**
 * Hook xử lý sự kiện click ra ngoài một element
 * @param {Object} ref - React ref của element
 * @param {Function} callback - Hàm thực thi khi click out
 */
export const useClickOutside = (ref, callback) => {
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (ref.current && !ref.current.contains(event.target)) {
        callback();
      }
    };
    
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [ref, callback]);
};
