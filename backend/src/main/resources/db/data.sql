INSERT INTO finance_period (id, year_value, month_value, quarter_value, period_label)
VALUES
    (1, 2025, 6, 2, '2025-06'),
    (2, 2025, 7, 3, '2025-07'),
    (3, 2025, 8, 3, '2025-08'),
    (4, 2025, 9, 3, '2025-09'),
    (5, 2025, 10, 4, '2025-10'),
    (6, 2025, 11, 4, '2025-11'),
    (7, 2025, 12, 4, '2025-12'),
    (8, 2026, 1, 1, '2026-01'),
    (9, 2026, 2, 1, '2026-02'),
    (10, 2026, 3, 1, '2026-03'),
    (11, 2026, 4, 2, '2026-04'),
    (12, 2026, 5, 2, '2026-05')
ON DUPLICATE KEY UPDATE
    quarter_value = VALUES(quarter_value),
    period_label = VALUES(period_label);

INSERT INTO finance_metric (period_id, metric_code, metric_name, metric_category, actual_value, unit)
VALUES
    (1, 'REVENUE', '收入', 'INCOME', 1180.00, '万元'),
    (1, 'COST', '成本', 'COST', 690.00, '万元'),
    (1, 'GROSS_PROFIT', '毛利', 'PROFIT', 490.00, '万元'),
    (1, 'OPERATING_EXPENSE', '经营费用', 'COST', 210.00, '万元'),
    (1, 'NET_PROFIT', '净利', 'PROFIT', 280.00, '万元'),
    (1, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 260.00, '万元'),
    (2, 'REVENUE', '收入', 'INCOME', 1220.00, '万元'),
    (2, 'COST', '成本', 'COST', 710.00, '万元'),
    (2, 'GROSS_PROFIT', '毛利', 'PROFIT', 510.00, '万元'),
    (2, 'OPERATING_EXPENSE', '经营费用', 'COST', 218.00, '万元'),
    (2, 'NET_PROFIT', '净利', 'PROFIT', 292.00, '万元'),
    (2, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 275.00, '万元'),
    (3, 'REVENUE', '收入', 'INCOME', 1280.00, '万元'),
    (3, 'COST', '成本', 'COST', 735.00, '万元'),
    (3, 'GROSS_PROFIT', '毛利', 'PROFIT', 545.00, '万元'),
    (3, 'OPERATING_EXPENSE', '经营费用', 'COST', 225.00, '万元'),
    (3, 'NET_PROFIT', '净利', 'PROFIT', 320.00, '万元'),
    (3, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 310.00, '万元'),
    (4, 'REVENUE', '收入', 'INCOME', 1310.00, '万元'),
    (4, 'COST', '成本', 'COST', 910.00, '万元'),
    (4, 'GROSS_PROFIT', '毛利', 'PROFIT', 400.00, '万元'),
    (4, 'OPERATING_EXPENSE', '经营费用', 'COST', 245.00, '万元'),
    (4, 'NET_PROFIT', '净利', 'PROFIT', 155.00, '万元'),
    (4, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 120.00, '万元'),
    (5, 'REVENUE', '收入', 'INCOME', 1260.00, '万元'),
    (5, 'COST', '成本', 'COST', 840.00, '万元'),
    (5, 'GROSS_PROFIT', '毛利', 'PROFIT', 420.00, '万元'),
    (5, 'OPERATING_EXPENSE', '经营费用', 'COST', 260.00, '万元'),
    (5, 'NET_PROFIT', '净利', 'PROFIT', 160.00, '万元'),
    (5, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', -85.00, '万元'),
    (6, 'REVENUE', '收入', 'INCOME', 1380.00, '万元'),
    (6, 'COST', '成本', 'COST', 790.00, '万元'),
    (6, 'GROSS_PROFIT', '毛利', 'PROFIT', 590.00, '万元'),
    (6, 'OPERATING_EXPENSE', '经营费用', 'COST', 235.00, '万元'),
    (6, 'NET_PROFIT', '净利', 'PROFIT', 355.00, '万元'),
    (6, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 330.00, '万元'),
    (7, 'REVENUE', '收入', 'INCOME', 1520.00, '万元'),
    (7, 'COST', '成本', 'COST', 850.00, '万元'),
    (7, 'GROSS_PROFIT', '毛利', 'PROFIT', 670.00, '万元'),
    (7, 'OPERATING_EXPENSE', '经营费用', 'COST', 270.00, '万元'),
    (7, 'NET_PROFIT', '净利', 'PROFIT', 400.00, '万元'),
    (7, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 390.00, '万元'),
    (8, 'REVENUE', '收入', 'INCOME', 1350.00, '万元'),
    (8, 'COST', '成本', 'COST', 780.00, '万元'),
    (8, 'GROSS_PROFIT', '毛利', 'PROFIT', 570.00, '万元'),
    (8, 'OPERATING_EXPENSE', '经营费用', 'COST', 238.00, '万元'),
    (8, 'NET_PROFIT', '净利', 'PROFIT', 332.00, '万元'),
    (8, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 300.00, '万元'),
    (9, 'REVENUE', '收入', 'INCOME', 1290.00, '万元'),
    (9, 'COST', '成本', 'COST', 760.00, '万元'),
    (9, 'GROSS_PROFIT', '毛利', 'PROFIT', 530.00, '万元'),
    (9, 'OPERATING_EXPENSE', '经营费用', 'COST', 242.00, '万元'),
    (9, 'NET_PROFIT', '净利', 'PROFIT', 288.00, '万元'),
    (9, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 255.00, '万元'),
    (10, 'REVENUE', '收入', 'INCOME', 1420.00, '万元'),
    (10, 'COST', '成本', 'COST', 820.00, '万元'),
    (10, 'GROSS_PROFIT', '毛利', 'PROFIT', 600.00, '万元'),
    (10, 'OPERATING_EXPENSE', '经营费用', 'COST', 248.00, '万元'),
    (10, 'NET_PROFIT', '净利', 'PROFIT', 352.00, '万元'),
    (10, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 340.00, '万元'),
    (11, 'REVENUE', '收入', 'INCOME', 1490.00, '万元'),
    (11, 'COST', '成本', 'COST', 855.00, '万元'),
    (11, 'GROSS_PROFIT', '毛利', 'PROFIT', 635.00, '万元'),
    (11, 'OPERATING_EXPENSE', '经营费用', 'COST', 255.00, '万元'),
    (11, 'NET_PROFIT', '净利', 'PROFIT', 380.00, '万元'),
    (11, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 360.00, '万元'),
    (12, 'REVENUE', '收入', 'INCOME', 1580.00, '万元'),
    (12, 'COST', '成本', 'COST', 900.00, '万元'),
    (12, 'GROSS_PROFIT', '毛利', 'PROFIT', 680.00, '万元'),
    (12, 'OPERATING_EXPENSE', '经营费用', 'COST', 265.00, '万元'),
    (12, 'NET_PROFIT', '净利', 'PROFIT', 415.00, '万元'),
    (12, 'OPERATING_CASH_FLOW', '经营现金流', 'CASH_FLOW', 405.00, '万元')
ON DUPLICATE KEY UPDATE
    metric_name = VALUES(metric_name),
    metric_category = VALUES(metric_category),
    actual_value = VALUES(actual_value),
    unit = VALUES(unit);

INSERT INTO finance_budget (period_id, metric_code, budget_value, unit)
VALUES
    (1, 'REVENUE', 1160.00, '万元'), (1, 'COST', 700.00, '万元'), (1, 'GROSS_PROFIT', 460.00, '万元'), (1, 'NET_PROFIT', 260.00, '万元'), (1, 'OPERATING_CASH_FLOW', 240.00, '万元'),
    (2, 'REVENUE', 1200.00, '万元'), (2, 'COST', 720.00, '万元'), (2, 'GROSS_PROFIT', 480.00, '万元'), (2, 'NET_PROFIT', 275.00, '万元'), (2, 'OPERATING_CASH_FLOW', 260.00, '万元'),
    (3, 'REVENUE', 1250.00, '万元'), (3, 'COST', 740.00, '万元'), (3, 'GROSS_PROFIT', 510.00, '万元'), (3, 'NET_PROFIT', 300.00, '万元'), (3, 'OPERATING_CASH_FLOW', 295.00, '万元'),
    (4, 'REVENUE', 1340.00, '万元'), (4, 'COST', 780.00, '万元'), (4, 'GROSS_PROFIT', 560.00, '万元'), (4, 'NET_PROFIT', 330.00, '万元'), (4, 'OPERATING_CASH_FLOW', 310.00, '万元'),
    (5, 'REVENUE', 1320.00, '万元'), (5, 'COST', 800.00, '万元'), (5, 'GROSS_PROFIT', 520.00, '万元'), (5, 'NET_PROFIT', 300.00, '万元'), (5, 'OPERATING_CASH_FLOW', 280.00, '万元'),
    (6, 'REVENUE', 1360.00, '万元'), (6, 'COST', 805.00, '万元'), (6, 'GROSS_PROFIT', 555.00, '万元'), (6, 'NET_PROFIT', 330.00, '万元'), (6, 'OPERATING_CASH_FLOW', 310.00, '万元'),
    (7, 'REVENUE', 1500.00, '万元'), (7, 'COST', 870.00, '万元'), (7, 'GROSS_PROFIT', 630.00, '万元'), (7, 'NET_PROFIT', 380.00, '万元'), (7, 'OPERATING_CASH_FLOW', 360.00, '万元'),
    (8, 'REVENUE', 1380.00, '万元'), (8, 'COST', 790.00, '万元'), (8, 'GROSS_PROFIT', 590.00, '万元'), (8, 'NET_PROFIT', 345.00, '万元'), (8, 'OPERATING_CASH_FLOW', 320.00, '万元'),
    (9, 'REVENUE', 1360.00, '万元'), (9, 'COST', 770.00, '万元'), (9, 'GROSS_PROFIT', 590.00, '万元'), (9, 'NET_PROFIT', 335.00, '万元'), (9, 'OPERATING_CASH_FLOW', 310.00, '万元'),
    (10, 'REVENUE', 1410.00, '万元'), (10, 'COST', 830.00, '万元'), (10, 'GROSS_PROFIT', 580.00, '万元'), (10, 'NET_PROFIT', 340.00, '万元'), (10, 'OPERATING_CASH_FLOW', 320.00, '万元'),
    (11, 'REVENUE', 1460.00, '万元'), (11, 'COST', 860.00, '万元'), (11, 'GROSS_PROFIT', 600.00, '万元'), (11, 'NET_PROFIT', 360.00, '万元'), (11, 'OPERATING_CASH_FLOW', 345.00, '万元'),
    (12, 'REVENUE', 1550.00, '万元'), (12, 'COST', 910.00, '万元'), (12, 'GROSS_PROFIT', 640.00, '万元'), (12, 'NET_PROFIT', 390.00, '万元'), (12, 'OPERATING_CASH_FLOW', 380.00, '万元')
ON DUPLICATE KEY UPDATE
    budget_value = VALUES(budget_value),
    unit = VALUES(unit);

INSERT INTO business_line_metric (period_id, business_line, revenue, cost, gross_profit, net_profit, unit)
VALUES
    (1, '企业服务', 720.00, 430.00, 290.00, 165.00, '万元'), (1, '订阅业务', 460.00, 260.00, 200.00, 115.00, '万元'),
    (2, '企业服务', 745.00, 440.00, 305.00, 174.00, '万元'), (2, '订阅业务', 475.00, 270.00, 205.00, 118.00, '万元'),
    (3, '企业服务', 780.00, 455.00, 325.00, 190.00, '万元'), (3, '订阅业务', 500.00, 280.00, 220.00, 130.00, '万元'),
    (4, '企业服务', 790.00, 560.00, 230.00, 88.00, '万元'), (4, '订阅业务', 520.00, 350.00, 170.00, 67.00, '万元'),
    (5, '企业服务', 760.00, 520.00, 240.00, 92.00, '万元'), (5, '订阅业务', 500.00, 320.00, 180.00, 68.00, '万元'),
    (6, '企业服务', 840.00, 490.00, 350.00, 210.00, '万元'), (6, '订阅业务', 540.00, 300.00, 240.00, 145.00, '万元'),
    (7, '企业服务', 930.00, 530.00, 400.00, 245.00, '万元'), (7, '订阅业务', 590.00, 320.00, 270.00, 155.00, '万元'),
    (8, '企业服务', 815.00, 480.00, 335.00, 198.00, '万元'), (8, '订阅业务', 535.00, 300.00, 235.00, 134.00, '万元'),
    (9, '企业服务', 775.00, 470.00, 305.00, 175.00, '万元'), (9, '订阅业务', 515.00, 290.00, 225.00, 113.00, '万元'),
    (10, '企业服务', 860.00, 505.00, 355.00, 210.00, '万元'), (10, '订阅业务', 560.00, 315.00, 245.00, 142.00, '万元'),
    (11, '企业服务', 900.00, 525.00, 375.00, 228.00, '万元'), (11, '订阅业务', 590.00, 330.00, 260.00, 152.00, '万元'),
    (12, '企业服务', 960.00, 555.00, 405.00, 252.00, '万元'), (12, '订阅业务', 620.00, 345.00, 275.00, 163.00, '万元')
ON DUPLICATE KEY UPDATE
    revenue = VALUES(revenue),
    cost = VALUES(cost),
    gross_profit = VALUES(gross_profit),
    net_profit = VALUES(net_profit),
    unit = VALUES(unit);

-- Event markers for AI analysis validation:
-- COST_GROWTH_FAST: 2025-09 cost jumps from 735.00 to 910.00.
-- CASH_FLOW_RISK: 2025-10 operating cash flow turns negative at -85.00.
-- BUDGET_OVERSPEND: 2025-09 cost exceeds budget by 130.00.
-- PROFIT_MARGIN_DROP: 2025-09 net profit margin drops sharply versus 2025-08.
