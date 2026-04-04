// src/app/admin/dashboard/page.tsx
"use client";

import { useQuery } from "@tanstack/react-query";
import { Card, Col, Row, Statistic, Spin, Typography } from "antd";
import { UserOutlined, ShopOutlined, CarOutlined } from "@ant-design/icons";
import api from "@/lib/axios";
import { ApiResponse } from "@/types/auth";
import { UserProfileResponse } from "@/types/user"; // Import interface

const { Title } = Typography;

export default function DashboardPage() {
  // Ép kiểu cho ApiResponse để lấy Intellisense cho 'profile'
  const { data: profile, isLoading } = useQuery<UserProfileResponse>({
    queryKey: ["admin-profile"],
    queryFn: async () => {
      const res = await api.get<ApiResponse<UserProfileResponse>>("/api/users/me");
      return res.data.data;
    },
  });

  if (isLoading) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <Spin size="large" description="Đang tải dữ liệu..." />
      </div>
    );
  }

  return (
    <div>
      {/* Bây giờ profile?.firstName sẽ có gợi ý code và không bị lỗi any */}
      <Title level={2}>Chào mừng trở lại, {profile?.firstName}!</Title>

      <Row gutter={[16, 16]} className="mt-6">
        <Col xs={24} sm={8}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Nhà hàng chờ duyệt"
              value={12}
              prefix={<ShopOutlined />}
              styles={{
                content: { color: "#cf1322" },
              }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic title="Shipper đang online" value={45} prefix={<CarOutlined />} styles={{ content: { color: "#cf1322" }}} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic title="Tổng người dùng" value={1200} prefix={<UserOutlined />} styles={{content: { color: "#cf1322" }}} />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
