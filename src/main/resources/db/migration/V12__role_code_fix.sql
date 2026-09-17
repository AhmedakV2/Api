UPDATE role SET code = 'VIEWER', description = 'Yalnizca okuma; agent oturumu acamaz'
WHERE code = 'VIWER';

UPDATE role SET description = 'Agent oturumu acma, kendi profil ve tercihlerini duzenleme'
WHERE code = 'USER';