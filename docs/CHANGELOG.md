**Unreleased**


[0.1.0] - 2026-03-26

Added:
- RefreshTokenService: extracted refresh token creation and refresh logic from AuthService

Changed:
- AuthService: removed refresh(), createRefreshToken(), and RefreshTokenRepository dependency; now delegates to RefreshTokenService
- PostService: added createPost() and getPostsBySub() methods (moved from SubService); added SubRepository and SubMemberRepository dependencies
- SubService: removed post-related methods (createPost, getPostsBySub) and dependencies (PostRepository, PostMapper); consolidated duplicate SubMemberRepository field into single field
- AuthController: refresh endpoint now routes through RefreshTokenService
- SubController: post creation and listing endpoints now route through PostService instead of SubService
- Updated AuthServiceTest, PostServiceTest, and SubServiceTest to reflect service responsibility changes