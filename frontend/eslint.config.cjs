/* eslint-env node */
const { FlatCompat } = require('@eslint/eslintrc');
const js = require('@eslint/js');
const vueParser = require('vue-eslint-parser');
const tsParser = require('@typescript-eslint/parser');

const compat = new FlatCompat({
    baseDirectory: __dirname,
    recommendedConfig: js.configs.recommended
});

module.exports = [
    {
        ignores: [
            'node_modules', 'dist', 'dist-ssr', 'coverage', '**/*.min.js', 'build',
            '.eslintrc.cjs', 'eslint.config.cjs', 'postcss.config.js'
        ]
    },
    ...compat.extends(
        'plugin:vue/vue3-essential',
        'eslint:recommended',
        'plugin:@typescript-eslint/recommended',
        '@vue/eslint-config-prettier'
    ),
    {
        files: ['**/*.vue'],
        languageOptions: {
            parser: vueParser,
            parserOptions: {
                parser: tsParser,
                ecmaVersion: 'latest',
                sourceType: 'module',
                extraFileExtensions: ['.vue']
            }
        }
    },
    {
        rules: {
            'vue/multi-word-component-names': 'off',
            'vue/no-reserved-component-names': 'off',
            'vue/component-tags-order': [
                'error',
                { order: ['script', 'template', 'style'] }
            ],
            '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
            '@typescript-eslint/no-explicit-any': 'warn',
            '@typescript-eslint/no-non-null-assertion': 'off'
        }
    }
];
