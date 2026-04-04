export interface ShipperResponse {
  id: string;
  vehicleInfo: string;
  licensePlate: string;
  isOnline: boolean;
  isBusy: boolean;
  approvalStatus: "PENDING" | "APPROVED" | "REJECTED";
  lat: number;
  lng: number;
  ratingAvg: number;
  reviewCount: number;
  updatedAt: string;
  user: {
    id: string;
    username: string;
    email: string;
    fullName: string;
    phone: string;
  };
}