export const ALLOWED_ROLES = ['ADMIN', 'STAFF', 'EMERGENCY_ENTITY', 'USER'];

export function normalizeRoles(claims = {}) {
  const roles = Array.isArray(claims.roles) ? claims.roles : [];
  return roles.map((role) => String(role).toUpperCase()).filter((role) => ALLOWED_ROLES.includes(role));
}

export function selectDashboardRole(roles = []) {
  return ALLOWED_ROLES.find((role) => roles.includes(role)) || null;
}
