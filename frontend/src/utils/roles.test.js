import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { normalizeRoles, selectDashboardRole } from './roles.js';

describe('normalizeRoles', () => {
  it('accepts Entra application roles used by the dashboards', () => {
    assert.deepEqual(normalizeRoles({ roles: ['admin', 'EMERGENCY_ENTITY'] }), ['ADMIN', 'EMERGENCY_ENTITY']);
  });
  it('rejects unknown roles and missing claims', () => {
    assert.deepEqual(normalizeRoles({ roles: ['UNKNOWN'] }), []);
    assert.deepEqual(normalizeRoles(), []);
  });
  it('selects a known dashboard role without granting USER by default', () => {
    assert.equal(selectDashboardRole(['USER']), 'USER');
    assert.equal(selectDashboardRole([]), null);
    assert.equal(selectDashboardRole(['UNKNOWN']), null);
  });
});
