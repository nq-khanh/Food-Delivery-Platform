"use client";

import React, { useState } from 'react';
import { Layout, Menu, Button, theme, Modal } from 'antd'; // Thêm Modal để xác nhận
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  UserOutlined,
  ShopOutlined,
  LogoutOutlined,
  ExclamationCircleFilled,
} from '@ant-design/icons';
import { useRouter, usePathname } from 'next/navigation';
import { logoutAction } from '@/actions/logout';

const { Header, Sider, Content } = Layout;
const { confirm } = Modal;

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const [collapsed, setCollapsed] = useState(false);
  const router = useRouter();
  const pathname = usePathname(); // Để highlight menu đúng trang hiện tại
  const { token: { colorBgContainer, borderRadiusLG } } = theme.useToken();

  // Hàm xử lý đăng xuất
 const handleLogout = () => {
    confirm({
      title: 'Xác nhận đăng xuất?',
      icon: <ExclamationCircleFilled />,
      content: 'Phiên làm việc của bạn sẽ kết thúc ngay lập tức.',
      okText: 'Đăng xuất',
      okType: 'danger',
      cancelText: 'Hủy',
      async onOk() {
        try {
          await logoutAction();
          router.replace("/login"); 
        } catch (error) {
          console.error("Logout failed", error);
        }
      },
    });
  };
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed}
        style={{
          display: 'flex',
          flexDirection: 'column',
          height: '100vh',
          position: 'sticky',
          top: 0,
          left: 0,
        }}
      >
        {/* Logo Section */}
        <div style={{ height: 32, marginTop: 16, marginBottom: 16 }}>
          <h1 className={`text-white font-bold text-center transition-all ${collapsed ? 'text-xs' : 'text-lg'}`}>
            {collapsed ? 'AD' : 'ADMIN'}
          </h1>
        </div>

        {/* Menu Section - Chiếm phần lớn không gian phía trên */}
        <div style={{ flex: 1, overflowY: 'auto' }}>
          <Menu
            theme="dark"
            mode="inline"
            selectedKeys={[pathname]}
            onClick={({ key }) => router.push(key)}
            items={[
              { key: '/admin/dashboard', icon: <DashboardOutlined />, label: 'Dashboard' },
              { key: '/admin/restaurants/pending', icon: <ShopOutlined />, label: 'Duyệt Nhà Hàng' },
              { key: '/admin/shippers/pending', icon: <UserOutlined />, label: 'Duyệt Shipper' },
            ]}
          />
        </div>

        {/* Bottom Section - Nút Logout nằm ở đây */}
        <div style={{ paddingBottom: 24, borderTop: '1px solid rgba(255,255,255,0.1)' }}>
          <Menu
            theme="dark"
            mode="inline"
            selectable={false}
            onClick={handleLogout}
            items={[
              { 
                key: 'logout', 
                icon: <LogoutOutlined />, 
                label: collapsed ? '' : 'Đăng xuất', 
                danger: true 
              },
            ]}
          />
        </div>
      </Sider>

      <Layout>
        <Header style={{ padding: 0, background: colorBgContainer, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{ fontSize: '16px', width: 64, height: 64 }}
            />
            <h2 className="text-lg font-semibold">Hệ Thống Quản Trị Food Delivery</h2>
          </div>
        </Header>
        
        <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280, background: colorBgContainer, borderRadius: borderRadiusLG }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}