import axios from 'axios';

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '');

const http = axios.create({
  baseURL: apiBaseUrl,
  timeout: 30000,
});

export default http;
