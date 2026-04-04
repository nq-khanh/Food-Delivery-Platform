import { createSlice } from "@reduxjs/toolkit";

interface UIState {
  isSidebarOpen: boolean;
  theme: 'light' | 'dark';
}

const initialState: UIState = {
  isSidebarOpen: true,
  theme: 'light'
};

const uiSlice = createSlice({
  name: "ui",
  initialState,
  reducers: {
    toggleSidebar: (state) => {
      state.isSidebarOpen = !state.isSidebarOpen;
    },
    setTheme: (state, action) => {
      state.theme = action.payload;
    }
  },
});

export const { toggleSidebar, setTheme } = uiSlice.actions;
export default uiSlice.reducer;