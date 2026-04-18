/**
 * Cloudflare Worker - APK Builder API
 *
 * 环境变量（CF Dashboard > Worker > Settings > Variables）:
 *   GITHUB_TOKEN   : GitHub PAT (repo + workflow scope)
 *   GITHUB_OWNER   : GitHub 用户名
 *   GITHUB_REPO    : 仓库名
 *   ALLOWED_ORIGIN : 前端域名，如 https://apkbuilder.pages.dev
 */

const GH = 'https://api.github.com';

export default {
  async fetch(request, env) {
    if (request.method === 'OPTIONS') return cors(new Response(null, { status: 204 }), env);
    const url = new URL(request.url);
    try {
      let res;
      if      (url.pathname === '/build'    && request.method === 'POST') res = await handleBuild(request, env);
      else if (url.pathname === '/status'   && request.method === 'GET')  res = await handleStatus(request, env);
      else if (url.pathname === '/download' && request.method === 'GET')  res = await handleDownload(request, env);
      else res = json({ error: 'Not found' }, 404);
      return cors(res, env);
    } catch (e) {
      return cors(json({ error: e.message }, 500), env);
    }
  }
};

async function handleBuild(request, env) {
  const { app_url, app_name, package_name, version_name, icon_url } = await request.json();
  if (!app_url || !app_name || !package_name || !version_name || !icon_url)
    return json({ error: 'Missing required fields' }, 400);
  const pkgRe = /^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*){2,}$/;
  if (!pkgRe.test(package_name))
    return json({ error: 'Invalid package name' }, 400);

  const r = await gh(env,
    `/repos/${env.GITHUB_OWNER}/${env.GITHUB_REPO}/actions/workflows/build.yml/dispatches`,
    { method: 'POST', body: JSON.stringify({
        ref: 'main',
        inputs: { app_url, app_name, package_name, version_name, icon_url }
    })}
  );
  if (r.status !== 204) return json({ error: 'Trigger failed', detail: await r.text() }, 500);

  await sleep(4000);

  const runs  = await (await gh(env,
    `/repos/${env.GITHUB_OWNER}/${env.GITHUB_REPO}/actions/workflows/build.yml/runs?per_page=1`
  )).json();
  const runId = runs.workflow_runs?.[0]?.id;
  if (!runId) return json({ error: 'Could not get run_id' }, 500);
  return json({ run_id: runId, status: 'queued' });
}

async function handleStatus(request, env) {
  const runId = new URL(request.url).searchParams.get('run_id');
  if (!runId) return json({ error: 'Missing run_id' }, 400);
  const data = await (await gh(env,
    `/repos/${env.GITHUB_OWNER}/${env.GITHUB_REPO}/actions/runs/${runId}`
  )).json();
  const result = { run_id: runId, status: data.status, conclusion: data.conclusion };
  if (data.status === 'completed' && data.conclusion === 'success') {
    const arts = await (await gh(env,
      `/repos/${env.GITHUB_OWNER}/${env.GITHUB_REPO}/actions/runs/${runId}/artifacts`
    )).json();
    const a = arts.artifacts?.[0];
    if (a) { result.artifact_id = a.id; result.artifact_name = a.name; }
  }
  return json(result);
}

async function handleDownload(request, env) {
  const runId = new URL(request.url).searchParams.get('run_id');
  if (!runId) return json({ error: 'Missing run_id' }, 400);
  const arts = await (await gh(env,
    `/repos/${env.GITHUB_OWNER}/${env.GITHUB_REPO}/actions/runs/${runId}/artifacts`
  )).json();
  const a = arts.artifacts?.[0];
  if (!a) return json({ error: 'Artifact not found' }, 404);
  const dl = await gh(env,
    `/repos/${env.GITHUB_OWNER}/${env.GITHUB_REPO}/actions/artifacts/${a.id}/zip`,
    { redirect: 'follow' }
  );
  if (!dl.ok) return json({ error: 'Download failed' }, 502);
  return new Response(dl.body, {
    headers: {
      'Content-Type': 'application/zip',
      'Content-Disposition': `attachment; filename="${a.name}.zip"`,
      'Cache-Control': 'no-store',
    }
  });
}

const gh = (env, path, opts = {}) =>
  fetch(`${GH}${path}`, {
    ...opts,
    headers: {
      Authorization: `Bearer ${env.GITHUB_TOKEN}`,
      Accept: 'application/vnd.github+json',
      'X-GitHub-Api-Version': '2022-11-28',
      'User-Agent': 'APK-Builder-CF-Worker/1.0',
      'Content-Type': 'application/json',
      ...(opts.headers || {}),
    }
  });

const json  = (d, s = 200) => new Response(JSON.stringify(d), {
  status: s, headers: { 'Content-Type': 'application/json' }
});
const sleep = ms => new Promise(r => setTimeout(r, ms));
const cors  = (res, env) => {
  const h = new Headers(res.headers);
  h.set('Access-Control-Allow-Origin',  env.ALLOWED_ORIGIN || '*');
  h.set('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  h.set('Access-Control-Allow-Headers', 'Content-Type');
  return new Response(res.body, { status: res.status, headers: h });
};
