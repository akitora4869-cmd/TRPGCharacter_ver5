# TRPGCharacter

Paper 1.20.1 / Java 17 向けの CoC第6版キャラクターシートプラグインです。

## 主な機能

- `/status` で探索者シートを本として開く
- `/status give` で右クリック可能な探索者シート本を受け取る
- STR / CON / POW / DEX / APP / SIZ / INT / EDU をクリックして変更
- 能力値は `[判定]` から「能力値×5」の 1d100 判定
- HP / MP / SAN初期値 / アイデア / 幸運 / 知識を自動計算
- 現在SANをプレイヤーごとに保存し、本から変更・SANチェック・SAN減少量入力
- HP / MPも最大値と現在値を分けてプレイヤーごとに保存
- 戦闘技能を除いたCoC第6版の技能を広く収録
- 技能名・初期値・分類を `skills.yml` から変更可能
- 技能値をクリックして変更・1d100判定
- プレイヤーごとに `players.yml` へ保存
- `/roll 1d100`、`/roll 2d6` などの通常ダイス
- GitHub ActionsでJARを自動ビルド

## コマンド

- `/status` : 探索者シートを開く
- `/status give` : 探索者シート本を受け取る
- `/status reload` : config.yml / skills.yml / players.yml を再読み込み（OP）
- `/roll <XdY>` : ダイスを振る

## skills.yml の編集

例:

```yaml
skills:
  spot_hidden:
    name: "目星"
    default: 25
    category: "探索"
```

能力値式も使えます。

```yaml
  own_language:
    name: "母国語"
    default: "EDU*5"
    category: "言語"
```

独自技能を追加する場合:

```yaml
  drone:
    name: "ドローン操作"
    default: 1
    category: "技術"
```

保存後、サーバー内で `/status reload` を実行してください。

## GitHubでJARを作る

このZIPを解凍し、中身をGitHubリポジトリの**ルート**へアップロードしてください。

正しい配置:

```text
リポジトリ/
├─ .github/
│  └─ workflows/
│     └─ build.yml
├─ pom.xml
├─ README.md
└─ src/
```

GitHubの `Actions` → `Build TRPGCharacter` から実行できます。
成功後、Artifacts の `TRPGCharacter-Paper-1.20.1` からJARを取得できます。

## サーバーへの導入

1. Paper 1.20.1 / Java 17 サーバーを停止
2. JARを `plugins` フォルダへ入れる
3. サーバー起動
4. `/status` で動作確認

初回起動時に以下が生成されます。

```text
plugins/TRPGCharacter/
├─ config.yml
├─ skills.yml
└─ players.yml
```

## 注意

`SAN初期値` は `POW×5` で自動計算されます。
`現在SAN` は初回のみ `POW×5` を使用し、一度変更するとプレイヤーごとに `players.yml` へ保存されます。
本の `[判定]` から現在SANを目標値にした1d100のSANチェックもできます。

## 現在HP / MP / SAN

派生値ページでは以下のように表示されます。

```text
HP 10 / 13 [変更]
MP 8 / 12  [変更]
SAN初期値 60
現在SAN 52 [変更] [判定] [減少]
```

`[減少]` を押して `3` と入力すると、現在SANから3を引いて保存します。
0未満にはなりません。

## HPダメージ・回復 / MP消費・回復

本の派生値ページから操作できます。

```text
HP 10 / 13 [変更] [-] [+]
MP  8 / 12 [変更] [-] [+]
```

- HP `[-]`: 入力した値だけ現在HPを減らします（最低0）
- HP `[+]`: 入力した値だけ現在HPを回復します（最大HPまで）
- MP `[-]`: 入力した値だけ現在MPを消費します（最低0）
- MP `[+]`: 入力した値だけ現在MPを回復します（最大MPまで）


## キャラクター名と個別サイドバー

本の基本能力値ページからキャラクター名を変更できます。

画面右側はプレイヤーごとに個別表示されます。

```text
TRPG Server
オンライン 3 / 20

山田 太郎
HP 10 / 13
MP  8 / 12
SAN 52 / 60
```

他のプレイヤーのキャラクター状態は混ざりません。
サーバー名は `config.yml` の `sidebar.server-name` から変更できます。
