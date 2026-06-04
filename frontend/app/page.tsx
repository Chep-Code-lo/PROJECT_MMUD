export default function HomePage() {
  return (
    <main className="min-h-screen bg-gray-100 text-gray-900 flex items-center justify-center">
      <div className="bg-white p-10 rounded-2xl shadow max-w-2xl text-center">
        <h1 className="text-4xl font-bold text-blue-700 mb-4">
          CloudAPI Security
        </h1>

        <p className="text-lg text-gray-700 mb-8">
          Cloud API-Based Network Application Security for Small Company Services
        </p>

        <div className="flex justify-center gap-4">
          <a
            href="/login"
            className="bg-blue-600 text-white px-6 py-3 rounded hover:bg-blue-700"
          >
            Đăng nhập
          </a>

          <a
            href="/register"
            className="bg-gray-200 text-gray-900 px-6 py-3 rounded hover:bg-gray-300"
          >
            Đăng ký
          </a>
        </div>
      </div>
    </main>
  );
}