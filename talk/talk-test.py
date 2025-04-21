from modelscope.pipelines import pipeline
from modelscope.utils.constant import Tasks
from modelscope import snapshot_download
model_id = snapshot_download('iic/SenseVoiceSmall')
print(model_id)

inference_pipeline = pipeline(
    task=Tasks.auto_speech_recognition,
    model=model_id,
    model_revision="master",
    device="cuda:0",)

rec_result = inference_pipeline('./recordedAudio.wav')
print(rec_result)