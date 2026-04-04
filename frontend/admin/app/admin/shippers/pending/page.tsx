"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Table, Tag, Button, Space, Typography, TableProps, message, Popconfirm, Card } from "antd";
import { shipperService } from "@/services/shipperService";
import { ShipperResponse } from "@/types/shipper";
import { CheckOutlined, CloseOutlined, CarOutlined } from "@ant-design/icons";

const { Title } = Typography;

export default function PendingShippersPage() {
  const queryClient = useQueryClient();

  // 1. Fetch danh sách Shipper
  const { data, isLoading } = useQuery({
    queryKey: ["pending-shippers"],
    queryFn: shipperService.getPending,
  });

  // 2. Mutation Duyệt
  const approveMutation = useMutation({
    mutationFn: shipperService.approve,
    onSuccess: (res) => {
      if (res.success) {
        message.success("Đã duyệt tài khoản Shipper!");
        queryClient.invalidateQueries({ queryKey: ["pending-shippers"] });
      }
    },
    onError: () => message.error("Lỗi khi duyệt Shipper"),
  });

  // 3. Mutation Từ chối
  const rejectMutation = useMutation({
    mutationFn: shipperService.reject,
    onSuccess: () => {
      message.warning("Đã từ chối đơn đăng ký Shipper.");
      queryClient.invalidateQueries({ queryKey: ["pending-shippers"] });
    },
  });

  const columns: TableProps<ShipperResponse>["columns"] = [
    {
      title: "Họ và Tên",
      dataIndex: ["user", "fullName"],
      key: "fullName",
      render: (text) => <span className="font-semibold text-blue-600">{text}</span>
    },
    { title: "Số điện thoại", dataIndex: ["user", "phone"], key: "phone" },
    { 
      title: "Phương tiện", 
      dataIndex: "vehicleInfo", 
      key: "vehicleInfo",
      render: (text) => <span><CarOutlined className="mr-2" />{text}</span>
    },
    { title: "Biển số xe", dataIndex: "licensePlate", key: "licensePlate" },
    { 
      title: "Trạng thái", 
      dataIndex: "approvalStatus", 
      render: (status) => <Tag color="gold">{status}</Tag> 
    },
    {
      title: "Thao tác",
      key: "action",
      render: (_, record) => (
        <Space size="middle">
          <Popconfirm
            title="Duyệt Shipper"
            description="Cho phép người này bắt đầu giao hàng?"
            onConfirm={() => approveMutation.mutate(record.id)}
            okText="Đồng ý"
            cancelText="Hủy"
          >
            <Button 
              type="primary" 
              icon={<CheckOutlined />} 
              loading={approveMutation.isPending}
              className="bg-green-600 border-none"
            >
              Duyệt
            </Button>
          </Popconfirm>

          <Popconfirm
            title="Từ chối đơn"
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
    <Card className="shadow-md">
      <Title level={3} className="mb-6">Yêu Cầu Đăng Ký Shipper</Title>
      <Table 
        dataSource={data} 
        columns={columns} 
        loading={isLoading} 
        rowKey="id"
        pagination={{ pageSize: 10 }}
      />
    </Card>
  );
}