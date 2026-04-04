// src/store/index.ts
import { configureStore } from "@reduxjs/toolkit";
import uiReducer from "./slices/uiSlice";
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux";

export const store = configureStore({
  reducer: {
    ui: uiReducer,
  },
});

// Export các Type để dùng trong Hooks
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

// Tạo Custom Hooks để dùng trong Component (Typed)
export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;