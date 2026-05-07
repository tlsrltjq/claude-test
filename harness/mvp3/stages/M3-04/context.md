# MVP3 M3-04 — Context

이전: M3-01·M3-02·M3-03 완료. `/dashboard` 는 mvp1 3단계에서 만들어졌고 이름/이메일/권한/상태 정도만 표시.

핵심: LEFT JOIN으로 프로필 없는 사용자 대비. ddl-auto=validate라 entity ↔ DB 일치 필요.

위치: `user/controller/DashboardController`, `user/dto/DashboardSelfView`, `templates/dashboard.html`.
