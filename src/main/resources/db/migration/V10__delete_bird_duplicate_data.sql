-- 도감 데이터 중 한국어 이름이 중복인 데이터 제거
-- 기존 17쌍 중복이었고, 각각 학명 기준으로 아종(subspecies) 데이터를 삭제하여 중복 제거함
-- ex) 2개의 매 데이터가 있을 때(Falco peregrinus vs.  Falco peregrinus japonensis), 아종 데이터(Falco peregrinus japonensis) 삭제

-- 1. bird_image에서 연관 데이터 삭제 (S3에서 실제 이미지 삭제 작업 진행하였음)
DELETE FROM bird_image
WHERE bird_id IN (
                  597, 557, 523, 445, 586, 570, 593, 521, 423, 506, 441, 443, 444, 518, 425, 553, 517
    );

-- 2. bird_habitat에서 연관 데이터 삭제
DELETE FROM bird_habitat
WHERE bird_id IN (
                  597, 557, 523, 445, 586, 570, 593, 521, 423, 506, 441, 443, 444, 518, 425, 553, 517
    );

-- 3. bird_residency에서 연관 데이터 삭제
DELETE FROM bird_residency
WHERE bird_id IN (
                  597, 557, 523, 445, 586, 570, 593, 521, 423, 506, 441, 443, 444, 518, 425, 553, 517
    );

-- 4. bird 테이블에서 삭제
DELETE FROM bird
WHERE id IN (
             597, 557, 523, 445, 586, 570, 593, 521, 423, 506, 441, 443, 444, 518, 425, 553, 517
    );
