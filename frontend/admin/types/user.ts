export interface UserProfileResponse {
  id: string;
  username: string;
  email: string;
  phone: string;
  firstName: string;
  lastName: string;
  avatarUrl: string;
  role: "USER" | "ADMIN" | "MERCHANT" | "SHIPPER";
  isVerified: boolean;
}