"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Table, Tag, Button, Space, Typography, TableProps, message, Popconfirm } from "antd";
import { restaurantService } from "@/services/restaurantService";
import { RestaurantResponse } from "@/types/restaurant";
import { CheckOutlined, CloseOutlined } from "@ant-design/icons";

const { Title } = Typography;

export default function PendingRestaurantsPage() {
  const queryClient = useQueryClient();

  // 1. Fetch dữ liệu
  const { data, isLoading } = useQuery({
    queryKey: ["pending-restaurants"],
    queryFn: restaurantService.getPending,
  });

  // 2. Mutation để Duyệt
  const approveMutation = useMutation({
    mutationFn: restaurantService.approve,
    onSuccess: (res) => {
      if (res.success) {
        message.success("Đã duyệt nhà hàng thành công!");
        // Làm tươi lại danh sách ngay lập tức
        queryClient.invalidateQueries({ queryKey: ["pending-restaurants"] });
      }
    },
    onError: () => message.error("Lỗi khi duyệt nhà hàng"),
  });

  // 3. Mutation để Từ chối
  const rejectMutation = useMutation({
    mutationFn: restaurantService.reject,
    onSuccess: () => {
      message.warning("đã từ chối nhà hàng.");
      queryClient.invalidateQueries({ queryKey: ["pending-restaurants"] });
    },
  });

  const columns: TableProps<RestaurantResponse>["columns"] = [
    { title: "Tên Nhà Hàng", dataIndex: "name", key: "name" },
    { title: "Chủ sở hữu", dataIndex: ["owner", "fullName"], key: "owner" },
    { title: "Số điện thoại", dataIndex: ["owner", "phone"], key: "phone" },
    { 
      title: "Trạng thái", 
      dataIndex: "approvalStatus", 
      render: (status: string) => <Tag color="blue">{status}</Tag> 
    },
    {
      title: "Thao tác",
      key: "action",
      render: (_, record) => (
        <Space size="middle">
          <Popconfirm
            title="Xác nhận duyệt"
            description="Bạn có chắc chắn muốn duyệt nhà hàng này?"
            onConfirm={() => approveMutation.mutate(record.id)}
            okText="Duyệt"
            cancelText="Hủy"
          >
            <Button 
              type="primary" 
              icon={<CheckOutlined />} 
              loading={approveMutation.isPending}
              className="bg-green-600"
            >
              Duyệt
            </Button>
          </Popconfirm>

          <Popconfirm
            title="Từ chối duyệt"
            onConfirm={() => rejectMutation.mutate(record.id)}
            okType="danger"
          >
            <Button 
              danger 
              icon={<CloseOutlined />}
              loading={rejectMutation.isPending}
            >
              Từ chối
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="shadow-md">
      <Title level={3} className="mb-6">Duyệt Nhà Hàng Mới</Title>
      <Table 
        dataSource={data} 
        columns={columns} 
        loading={isLoading} 
        rowKey="id"
        pagination={{ pageSize: 10 }}
      />
    </div>
  );
}