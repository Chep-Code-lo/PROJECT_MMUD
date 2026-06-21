import axios from "axios";
import { tokenStorage } from "./tokenStorage";

const configuredApiUrl = process.env.NEXT_PUBLIC_API_URL?.trim();

const axiosClient = axios.create({
  ...(configuredApiUrl ? { baseURL: configuredApiUrl } : {}),
  headers: {
    "Content-Type": "application/json",
  },
});

axiosClient.interceptors.request.use((config) => {
  const token = tokenStorage.getToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      tokenStorage.removeToken();

      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);

export default axiosClient;
