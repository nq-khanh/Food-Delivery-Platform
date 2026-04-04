// hooks là một phần quan trọng trong việc sử dụng Redux trong ứng dụng React của bạn. Chúng giúp bạn kết nối các component của bạn với Redux store một cách dễ dàng và hiệu quả. Dưới đây là một số hooks phổ biến mà bạn có thể sử dụng khi làm việc với Redux trong ứng dụng React của bạn:
// 1. useSelector: Đây là một hook được cung cấp bởi thư viện react-redux, cho phép bạn truy cập vào state của Redux store trong các component của bạn. Bạn có thể sử dụng useSelector để lấy dữ liệu từ store và sử dụng nó trong component của bạn. Ví dụ:
// import { useSelector } from 'react-redux';
// const myData = useSelector((state) => state.mySlice.myData);
// 2. useDispatch: Đây là một hook được cung cấp bởi thư viện react-redux, cho phép bạn dispatch các action đến Redux store từ các component của bạn. Bạn có thể sử dụng useDispatch để gửi các action đến store và cập nhật state. Ví dụ:
// import { useDispatch } from 'react-redux';
// const dispatch = useDispatch();
// const handleButtonClick = () => {
//   dispatch(myAction());
// };
// 3. useStore: Đây là một hook được cung cấp bởi thư viện react-redux, cho phép bạn truy cập trực tiếp vào Redux store trong các component của bạn. Bạn có thể sử dụng useStore để lấy store và thực hiện các thao tác trực tiếp trên store nếu cần thiết. Ví dụ:
// import { useStore } from 'react-redux';
// const store = useStore();
// 4. useSelector và useDispatch cũng có thể được sử dụng kết hợp với nhau để truy cập vào state và dispatch các action trong cùng một component. Ví dụ:
// import { useSelector, useDispatch } from 'react-redux';
// const myData = useSelector((state) => state.mySlice.myData);
// const dispatch = useDispatch();
// const handleButtonClick = () => {
//   dispatch(myAction());
// };
// Tóm lại, hooks là một phần quan trọng trong việc sử dụng Redux trong ứng dụng React của bạn. Chúng giúp bạn kết nối các component của bạn với Redux store một cách dễ dàng và hiệu quả, cho phép bạn truy cập vào state và dispatch các action một cách linh hoạt và tiện lợi. Bằng cách sử dụng các hooks này, bạn có thể xây dựng các component React mạnh mẽ và dễ bảo trì khi làm việc với Redux trong ứng dụng của bạn.
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux'; // Đây là phần import các hook từ thư viện react-redux. useDispatch và useSelector là hai hook quan trọng để kết nối các component của bạn với Redux store. TypedUseSelectorHook là một kiểu được sử dụng để định nghĩa kiểu cho useSelector, giúp đảm bảo rằng khi bạn sử dụng useSelector trong các component của bạn, bạn sẽ có kiểu chính xác cho state mà bạn đang truy cập từ Redux store.
import type { RootState, AppDispatch } from './index'; // Đây là phần import các kiểu RootState và AppDispatch từ file index.ts của bạn. RootState đại diện cho toàn bộ state của ứng dụng, trong khi AppDispatch đại diện cho kiểu của hàm dispatch từ Redux store của bạn. Bằng cách import các kiểu này, bạn có thể sử dụng chúng để định nghĩa kiểu cho các hook useSelector và useDispatch của bạn, giúp đảm bảo rằng bạn có kiểu chính xác khi làm việc với state và dispatch trong các component của bạn.

export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
export const useAppDispatch = () => useDispatch<AppDispatch>();
// Tóm lại, đoạn code này định nghĩa hai hook tùy chỉnh là useAppSelector và useAppDispatch để kết nối các component của bạn với Redux store một cách dễ dàng và hiệu quả. useAppSelector sử dụng kiểu RootState để đảm bảo rằng khi bạn sử dụng nó trong các component của bạn, bạn sẽ có kiểu chính xác cho state mà bạn đang truy cập từ Redux store. useAppDispatch sử dụng kiểu AppDispatch để đảm bảo rằng khi bạn sử dụng nó trong các component của bạn, bạn sẽ có kiểu chính xác cho hàm dispatch từ Redux store của bạn. Bằng cách sử dụng các hook này, bạn có thể xây dựng các component React mạnh mẽ và dễ bảo trì khi làm việc với Redux trong ứng dụng của bạn.
// Đây là cách sử dụng hooks tùy chỉnh useAppSelector và useAppDispatch trong một component React của bạn:
/*
import React from 'react';
import { useAppSelector, useAppDispatch } from './hooks'; // Import các hook tùy chỉnh từ file hooks.ts của bạn
import { myAction } from './slices/mySlice'; // Import action từ slice của bạn

const MyComponent = () => {
  const myData = useAppSelector((state) => state.mySlice.myData); // Sử dụng useAppSelector để truy cập vào state từ Redux store
  const dispatch = useAppDispatch(); // Sử dụng useAppDispatch để lấy hàm dispatch từ Redux store

    const handleButtonClick = () => {
    dispatch(myAction()); // Sử dụng dispatch để gửi action đến Redux store
    };
    
    return (
    <div>
        <p>My Data: {myData}</p> { Hiển thị dữ liệu từ Redux store }
        <button onClick={handleButtonClick}>Dispatch Action</button> { Gọi hàm handleButtonClick khi nút được nhấn đen dispatch action đến Redux store }
    </div>
    );
};
export default MyComponent;
*/
// Trong ví dụ này, MyComponent sử dụng useAppSelector để truy cập vào state myData từ Redux store và hiển thị nó trong component. Nó cũng sử dụng useAppDispatch để lấy hàm dispatch và gửi một action (myAction) đến Redux store khi nút được nhấn. Bằng cách sử dụng các hook tùy chỉnh này, bạn có thể dễ dàng kết nối các component của bạn với Redux store và quản lý state một cách hiệu quả trong ứng dụng của bạn.
