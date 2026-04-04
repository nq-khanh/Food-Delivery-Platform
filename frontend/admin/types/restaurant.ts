export interface OwnerInfo {
  id: string;
  username: string;
  email: string;
  fullName: string;
  phone: string;
}

export interface RestaurantResponse {
  id: string;
  name: string;
  address: string;
  lat: number;
  lng: number;
  description: string;
  logoUrl: string;
  approvalStatus: "PENDING" | "APPROVED" | "REJECTED";
  ratingAvg: number;
  reviewCount: number;
  isActive: boolean;
  createdAt: string;
  owner: OwnerInfo;
}