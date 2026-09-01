import { Route, Routes } from "react-router-dom";

import LandingPage from "./pages/LandingPage";
import LoginPage from "./pages/LoginPage";
import PreferencesPage from "./pages/PreferencesPage";
import RecommendationsPage from "./pages/RecommendationsPage";
import RegisterPage from "./pages/RegisterPage";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<LoginPage />} />
      {/* Protected routes */}
      <Route element={<ProtectedRoute />}>
        <Route path="/preferences" element={<PreferencesPage />} />
      </Route>
      <Route element={<ProtectedRoute />}>
        <Route
          path="/recommendations"
          element={<RecommendationsPage />}
        />
      </Route>
    </Routes>
  );
}

export default App;