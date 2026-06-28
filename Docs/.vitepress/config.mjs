import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Pakr',
  description: 'One-click website to APK packaging',
  lang: 'zh-CN',
  lastUpdated: true,
  cleanUrls: true,
  ignoreDeadLinks: true,
  themeConfig: {
    siteTitle: 'Pakr Docs',
    nav: [
      { text: '中文', link: '/' },
      { text: 'English', link: '/en/' },
      { text: '在线体验', link: 'https://apk.091224.xyz' }
    ],
    sidebar: {
      '/': [
        {
          text: '指南',
          items: [
            { text: '介绍', link: '/guide/introduction' },
            { text: '快速开始', link: '/guide/quickstart' },
            { text: '部署说明', link: '/guide/deploy' }
          ]
        },
        {
          text: '参考',
          items: [
            { text: '功能特性', link: '/reference/features' },
            { text: '常见问题', link: '/reference/faq' }
          ]
        }
      ],
      '/en/': [
        {
          text: 'Guide',
          items: [
            { text: 'Introduction', link: '/en/guide/introduction' },
            { text: 'Quick Start', link: '/en/guide/quickstart' },
            { text: 'Deployment Notes', link: '/en/guide/deploy' }
          ]
        },
        {
          text: 'Reference',
          items: [
            { text: 'Features', link: '/en/reference/features' },
            { text: 'FAQ', link: '/en/reference/faq' }
          ]
        }
      ]
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/ZSFan888/APK' }
    ],
    outline: {
      level: [2, 3],
      label: '页面导航'
    },
    docFooter: {
      prev: '上一页',
      next: '下一页'
    }
  }
})
