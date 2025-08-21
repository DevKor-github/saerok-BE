INSERT INTO bird (id, korean_name, scientific_name, scientific_author, scientific_year, phylum_eng, phylum_kor, class_eng, class_kor, order_eng, order_kor, family_eng, family_kor, genus_eng, genus_kor, species_eng, species_kor, description, description_source, description_is_ai_generated, nibr_url, body_length_cm) VALUES ('603', '집비둘기', 'Columba livia domestica', 'Gmelin', '1789', 'Chordata', '척삭동물문', 'Aves', '조강', 'Columbiformes', '비둘기목', 'Columbidae', '비둘기과', 'Columba', '비둘기속', 'livia', '바위비둘기', '집비둘기는 바위비둘기에서 가축화되어 전 세계로 퍼진 아종으로, 한국에서는 16세기 문헌에도 기록이 남아 있다. 몸길이는 약 30~35cm, 날개 길이는 60~70cm이며 무게는 300~400g 정도로 바위비둘기보다 작고, 기본적으로 회색 몸통과 목의 녹색·보라색 광택, 검은 날개띠를 가지지만 가축화 과정에서 흰색, 갈색, 붉은빛 등 다양한 깃털 색과 무늬가 나타난다. 둥근 이마와 튀어나온 눈이 특징이며, 일부 개체는 볏이나 발 깃털 같은 변이를 보인다. 절벽을 서식지로 하던 조상과 달리 오늘날에는 건물 틈, 교량, 옥상 등 인간이 만든 구조물에 둥지를 틀며, 곡물과 음식물쓰레기를 먹고 먹이 조건만 맞으면 계절에 상관없이 연중 번식한다. 도시 환경에 적응하면서 개체 수가 급증해 산성 배설물로 건축물과 차량에 피해를 주고, 쓰레기를 뜯어 미관을 해치는 등 공해 문제를 일으킨다. 한국에서는 1960년대 이후 행사와 사육을 통해 크게 늘었고, 1980년대 아시안게임과 올림픽에서 수천 마리가 방생된 기록이 있으며, 현재는 일부 지자체에서 먹이 주기 금지 등 개체 수 관리 대책을 시행하고 있다.', '한국어, 영문 위키피디아 (https://ko.wikipedia.org/wiki, https://en.wikipedia.org/)', 'True', null, '32.5');

-- bird_image
INSERT INTO bird_image (id,bird_id,object_key,original_url,order_index,is_thumb) VALUES (nextval('bird_image_seq'),603,'raw/3c439fe8-bdaa-4621-8572-c2fccd9b91e3.jpg','https://upload.wikimedia.org/wikipedia/commons/e/ed/Feral_Pigeon_OCNJ.jpg',0,TRUE);

-- bird_habitat
INSERT INTO bird_habitat (id,bird_id,habitat_type) VALUES (nextval('bird_habitat_seq'),603,'RESIDENTIAL');
INSERT INTO bird_habitat (id,bird_id,habitat_type) VALUES (nextval('bird_habitat_seq'),603,'ARTIFICIAL');

-- bird_residency
INSERT INTO bird_residency (id,bird_id,residency_type_id,rarity_type_id,month_bitmask) VALUES (nextval('bird_residency_seq'),603,(SELECT id FROM residency_type WHERE code='RESIDENT'),(SELECT id FROM rarity_type WHERE code='COMMON'),NULL);

-- bird_seq를 올바른 값으로 보정
SELECT setval('bird_seq', (SELECT MAX(id) FROM bird));