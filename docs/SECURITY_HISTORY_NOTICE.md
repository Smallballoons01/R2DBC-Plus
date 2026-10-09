# 历史凭据清理说明

本项目曾将数据库地址与密码硬编码在测试配置中，并提交到了 Git 历史。
现已通过重写历史完成脱敏。

## 已完成

- 所有历史提交中的凭据已替换为`${R2DBC_TEST_*}` 环境变量占位符
- 远程 `main` 已强制推送到重写后的历史
- 新克隆的仓库中已扫描不到任何凭据

## 需要注意的历史变更

重写历史导致**所有 commit hash 均已变更**：

|旧 hash | 新 hash |
|---------|---------|
| `467dd13` | `8443fa1` |
| `04f33ce` | `6d14dcd` |
| `c837450` | `e18ec1a` |

如果你此前克隆过本项目，请改用：

```bash
# 移除旧 remote 后重新克隆
git remote set-url origin https://github.com/Smallballoons01/R2DBC-Plus.git
git fetch origin
git reset --hard origin/main
```

由于新旧历史无共同祖先，直接 `git pull` 会产生冲突。

## ⚠️ 仍需人工处理的事项

GitHub 的 force push **不会立即清除悬空对象（dangling objects）**。
在 GitHub 完成后台垃圾回收之前，已知 SHA 的旧提交仍可能通过
以下途径访问到：

- 直接按 SHA 访问提交页面
- GitHub API：`https://api.github.com/repos/Smallballoons01/R2DBC-Plus/commits/<旧SHA>`

**因此必须做的事：**

1. **立即轮换该数据库账号的密码**。这是最关键的一步 ——
   清除历史并不能让已被抓取的内容失效。
2. 若该数据库仍在使用中，评估是否存在被恶意访问的可能
3. 如需彻底清除 GitHub 上的悬空对象，可向 GitHub Support 提交请求：
   https://support.github.com/contact
   说明"需要清除已 force push 的旧历史中的敏感数据"

> 参考：GitHub 官方文档说明，force push 后旧的 commit 通常在数天到数周内
> 因后台 GC 而变得不可访问，但期间仍可通过 SHA 访问。
> https://docs.github.com/en/repositories/working-with-files/managing-large-files/about-large-files-on-github

## 未来的约定

为避免再次发生，仓库已采取以下措施：

- `SECURITY.md` 明确规定仓库内不得保存任何真实凭据
- `.gitignore` 覆盖 `.env`、`*.pem`、`*.key`、`application-local.yml` 等
- 配置模板文件仅使用 `${VAR}` 占位符
- CI 使用的凭据仅通过 GitHub Secrets 注入